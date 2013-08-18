
# General
import datetime
import json
import logging
import time

# Library
from django.conf import settings
from django.http import HttpResponse

# Project
from api.models import PHONE_TYPE_ANDROID, PHONE_TYPES_DICT
from api.models import Phone, PushProfileAndroid, PushAlert, Computer, Link, Pin

# Environment
logger = logging.getLogger(__name__)


# Helper functions

def _return_response(response_dict, status=200):
    response = json.dumps(response_dict)
    return HttpResponse(response, status=status, content_type="application/javascript")

def _return_error_response(error):
    response_dict = {'error': error}
    return _return_response(response_dict, status=400)


### POST REQUESTS BEGIN ###

def phone_register(request):
    logger.debug("phone_register(phone_uuid=%s, phone_type=%s)" % (
        request.POST.get('phone_uuid'),
        request.POST.get('phone_type'),
    ))
    
    if request.method != 'POST':
        return _return_error_response('POST required for this method')
    
    phone_uuid = request.POST.get('phone_uuid')
    if not phone_uuid:
        return _return_error_response('No phone_uuid provided')
    
    phone_type_str = request.POST.get('phone_type')
    if not phone_type_str:
        return _return_error_response('No phone_type provided')
    
    phone_type = None
    for type_id, type_str in PHONE_TYPES_DICT.iteritems():
        if phone_type_str == type_str:
            phone_type = type_id
    if phone_type is None:
        return _return_error_response('Invalid phone_type provided')
    
    phone, created = Phone.objects.get_or_create(phone_uuid=phone_uuid, phone_type=phone_type)
    
    response_dict = {'status': True}
    return _return_response(response_dict)


def phone_c2dm(request):
    logger.debug("phone_c2dm(phone_uuid=%s, c2dm_token=%s)" % (
        request.POST.get('phone_uuid'),
        request.POST.get('c2dm_token'),
    ))
    
    if request.method != 'POST':
        return _return_error_response('POST required for this method')
    
    phone_uuid = request.POST.get('phone_uuid')
    if not phone_uuid:
        return _return_error_response('No phone_uuid provided')
    
    c2dm_token = request.POST.get('c2dm_token')
    if not c2dm_token:
        return _return_error_response('No c2dm_token provided')
    
    try:
        phone = Phone.objects.get(phone_uuid=phone_uuid)
    except Phone.DoesNotExist:
        return _return_error_response('No phone with that phone_uuid - call /phone/register first!')
    
    if phone.phone_type != PHONE_TYPE_ANDROID:
        return _return_error_response("This method is only for Android phones!")
    
    PushProfileAndroid.update_push_profile(phone=phone, c2dm_token=c2dm_token)
    
    response_dict = {'status': True}
    return _return_response(response_dict)


def computer_register(request):
    ip_address = request.META.get('REMOTE_ADDR', '')
    ua_string = request.META.get('HTTP_USER_AGENT', '')
    
    logger.debug("computer_register() - IP: %s UA: %s" % (
        ip_address,
        ua_string,
    ))
    
    if request.method != 'POST':
        return _return_error_response('POST required for this method')
    
    computer = Computer.register(ip_address, ua_string)
    logger.info("Registered computer with hash=%s from IP: %s with UA string: %s" % (computer.hash, ip_address, ua_string))
    
    response_dict = {'hash': computer.hash}
    return _return_response(response_dict)


def pin_request(request):
    logger.debug("pin_request(phone_uuid=%s)" % (
        request.POST.get('phone_uuid'),
    ))
    
    if request.method != 'POST':
        return _return_error_response('POST required for this method')
    
    phone_uuid = request.POST.get('phone_uuid')
    if not phone_uuid:
        return _return_error_response('No phone_uuid provided')
    
    try:
        phone = Phone.objects.get(phone_uuid=phone_uuid)
    except Phone.DoesNotExist:
        return _return_error_response('No phone with that phone_uuid - call /phone/register first!')
    
    pin = Pin.get_pin(phone)
    
    response_dict = {'pin_code': pin.pin_code}
    return _return_response(response_dict)


def pin_resolve(request):
    ip_address = request.META.get('REMOTE_ADDR', '')
    ua_string = request.META.get('HTTP_USER_AGENT', '')
    
    logger.debug("pin_resolve(hash=%s) - IP: %s UA: %s" % (
        request.POST.get('hash'),
        ip_address,
        ua_string,
    ))
    
    if request.method != 'POST':
        return _return_error_response('POST required for this method')
    
    hash = request.POST.get('hash')
    if not hash:
        return _return_error_response('No hash provided')
    
    pin_code = request.POST.get('pin_code')
    if not pin_code:
        return _return_error_response('No pin_code provided')
    
    try:
        computer = Computer.objects.get(hash=hash)
    except Computer.DoesNotExist:
        return _return_error_response('No such computer with specified hash')
    
    result = Pin.resolve_pin(pin_code, computer)
    
    # TODO: throttle to prevent hacking attempts
    if not result:
        return _return_error_response('Unable to resolve pin_code to a phone')
    
    response_dict = {'status': True}
    return _return_response(response_dict)


def push(request):
    ip_address = request.META.get('REMOTE_ADDR', '')
    ua_string = request.META.get('HTTP_USER_AGENT', '')
    
    logger.debug("push(hash=%s) - IP: %s UA: %s" % (
        request.POST.get('hash'),
        ip_address,
        ua_string,
    ))
    
    if request.method != 'POST':
        return _return_error_response('POST required for this method')
    
    hash = request.POST.get('hash')
    if not hash:
        return _return_error_response('No hash provided')
    
    try:
        computer = Computer.objects.get(hash=hash)
    except Computer.DoesNotExist:
        return _return_error_response('No such computer with specified hash')
    
    # Dem QueryDict objects...
    # https://docs.djangoproject.com/en/dev/ref/request-response/#querydict-objects
    data = {k: v[0] for k, v in request.POST.iterlists()}
    del data['hash']

    messages_sent = 0
    
    links = Link.objects.filter(computer=computer)
    for link in links:
        phone = link.phone
        try:
            alert_id = PushAlert.send_push(phone, data)
            if alert_id:
                messages_sent += 1
        except PushException, e:
            # Silently fail
            logger.error("PushException encountered while trying to send message to phone %s: %s" % (phone, e))
            pass
    
    response_dict = {
        'status': True,
        'messages_sent': messages_sent,
    }
    return _return_response(response_dict)
    

### POST REQUESTS END ###

### GET REQUESTS BEGIN ###

def phone_status(request):
    logger.debug("phone_status(phone_uuid=%s)" % (
        request.GET.get('phone_uuid'),
    ))
    
    if request.method != 'GET':
        return _return_error_response('GET required for this method')
    
    phone_uuid = request.GET.get('phone_uuid')
    if not phone_uuid:
        return _return_error_response('No phone_uuid provided')
    
    try:
        phone = Phone.objects.get(phone_uuid=phone_uuid)
    except Phone.DoesNotExist:
        return _return_error_response('No phone with that phone_uuid - call /phone/register first!')
    
    response_dict = {}
    
    response_dict['phone_type'] = PHONE_TYPES_DICT[phone.phone_type]
    
    if phone.phone_type == PHONE_TYPE_ANDROID:
        try:
            push_profile = PushProfileAndroid.objects.get(phone=phone)
            response_dict['c2dm_token'] = push_profile.c2dm_token
            response_dict['c2dm_token_updated'] = time.mktime(push_profile.updated.timetuple())
        except PushProfileAndroid.DoesNotExist:
            response_dict['c2dm_token'] = None
            response_dict['c2dm_token_updated'] = None
    
    computers = []
    links = Link.objects.filter(phone=phone)
    for link in links:
        computer = link.computer
        computer_dict = {
            'hash': computer.hash,
            'created': time.mktime(computer.created.timetuple()),
        }
        computers.append(computer_dict)
    
    response_dict['computers'] = computers
    
    return _return_response(response_dict)


def computer_status(request):
    ip_address = request.META.get('REMOTE_ADDR', '')
    ua_string = request.META.get('HTTP_USER_AGENT', '')
    
    logger.debug("computer_status(hash=%s) - IP: %s UA: %s" % (
        request.GET.get('hash'),
        ip_address,
        ua_string,
    ))
    
    if request.method != 'GET':
        return _return_error_response('GET required for this method')
    
    hash = request.GET.get('hash')
    if not hash:
        return _return_error_response('No hash provided')
    
    try:
        computer = Computer.objects.get(hash=hash)
    except Computer.DoesNotExist:
        return _return_error_response('No computer with that hash!')
    
    response_dict = {}
    
    phones = []
    links = Link.objects.filter(computer=computer)
    for link in links:
        phone = link.phone
        phone_dict = {
            'phone_uuid': phone.phone_uuid,
            'created': time.mktime(phone.created.timetuple()),
        }
        phones.append(phone_dict)
    
    response_dict['phones'] = phones
    
    return _return_response(response_dict)


def pin_status(request):
    logger.debug("pin_status(phone_uuid=%s, pin_code=%s)" % (
        request.GET.get('phone_uuid'),
        request.GET.get('pin_code'),
    ))
    
    if request.method != 'GET':
        return _return_error_response('GET required for this method')
    
    phone_uuid = request.GET.get('phone_uuid')
    if not phone_uuid:
        return _return_error_response('No phone_uuid provided')
    
    try:
        phone = Phone.objects.get(phone_uuid=phone_uuid)
    except Phone.DoesNotExist:
        return _return_error_response('No phone with that phone_uuid - call /phone/register first!')
    
    pin_code = request.GET.get('pin_code')
    if not pin_code:
        return _return_error_response('No pin_code provided')
    
    try:
        pin = Pin.objects.get(phone=phone, pin_code=pin_code)
    except Pin.DoesNotExist:
        return _return_error_response('Invalid pin - call /pin/request first!')
    
    if pin.computer:
        response_dict = {'resolved': True}
        return _return_response(response_dict)
    
    if not pin.active:
        return _return_error_response('Pin must have timed out - call /pin/request to request a new one.')
    
    response_dict = {'resolved': False}
    return _return_response(response_dict)
    

### GET REQUESTS END ###
