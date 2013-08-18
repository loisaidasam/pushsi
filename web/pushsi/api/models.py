
import datetime
import hashlib
import json
import logging
import random
import time

from django.conf import settings
from django.db import models

import api.push as api_push

from api.models_static import *

logger = logging.getLogger(__name__)


class Phone(models.Model):
    phone_uuid = models.CharField(max_length=255, unique=True) # Generate this on the phone for uniqueness
    phone_type = models.IntegerField(choices=PHONE_TYPES)
    created = models.DateTimeField(auto_now_add=True)
    
    def __unicode__(self):
        return "%s (%s)" % (self.phone_uuid, PHONE_TYPES_DICT[self.phone_type])


class PushProfileAndroid(models.Model):
    phone = models.OneToOneField(Phone)
    c2dm_token = models.CharField(max_length=1024, blank=True, null=True)
    created = models.DateTimeField(auto_now_add=True)
    updated = models.DateTimeField(auto_now=True)
    
    def __unicode__(self):
        return "%s - cdm_token=%s" % (self.phone, self.c2dm_token)
    
    @staticmethod
    def update_push_profile(phone, c2dm_token):
        push_profile, created = PushProfileAndroid.objects.get_or_create(phone=phone)
        push_profile.c2dm_token = c2dm_token
        push_profile.save()
        return push_profile

        
# TODO: someday?
#class PushProfileIphone(models.Model)
#   phone = models.OneToOneField(Phone)


class PushAlert(models.Model):
    phone = models.ForeignKey(Phone)
    message = models.TextField()
    external_id = models.CharField(max_length=255)
    created = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return "%s - external_id=%s" % (self.phone, self.external_id)
    
    @staticmethod
    def send_push(phone, data):
        logger.info("Sending message to phone %s..." % phone)

        payload = json.dumps(data)
        
        # Android
        if phone.phone_type == PHONE_TYPE_ANDROID:
            try:
                push_profile = PushProfileAndroid.objects.get(phone=phone)
                push_id = api_push.send_push(
                    PHONE_TYPE_ANDROID,
                    payload, 
                    c2dm_token=push_profile.c2dm_token
                )
                alert = PushAlert.objects.create(
                    phone=phone,
                    message=payload,
                    external_id=push_id,
                )
                return alert.id
            except PushProfileAndroid.DoesNotExist:
                pass
        
        # iPhone (later, maybe?)
        
        return None


class Computer(models.Model):
    hash = models.CharField(max_length=32, unique=True)
    created = models.DateTimeField(auto_now_add=True)
    phones = models.ManyToManyField(Phone, through='Link')
    
    def __unicode__(self):
        return self.hash
    
    @staticmethod
    def register(ip_address, ua_string):
        while True:
            
            rando_thing = settings.RANDOM_THINGS[random.randint(0, len(settings.RANDOM_THINGS)-1)]
            hash_input = "%s:%s:%s:%s" % (ip_address, ua_string, time.time(), rando_thing)
            hash = hashlib.md5(hash_input).hexdigest()
            
            try:
                return Computer.objects.create(hash=hash)
            except:
                pass


class Link(models.Model):
    phone = models.ForeignKey(Phone)
    computer = models.ForeignKey(Computer)
    
    class Meta:
        unique_together = ('phone', 'computer')
    

class Pin(models.Model):
    pin_code = models.CharField(max_length=4)
    active = models.BooleanField(default=True)
    phone = models.ForeignKey(Phone)
    computer = models.ForeignKey(Computer, blank=True, null=True)
    created = models.DateTimeField(auto_now_add=True)
    matched = models.DateTimeField(blank=True, null=True)
    
    class Meta:
        unique_together = ('phone', 'pin_code')
    
    def __unicode__(self):
        return "pin=%s phone=%s active=%s" % (self.pin, self.phone, (self.active and 'Y' or 'N'))
    
    @staticmethod
    def get_pin(phone):
        # See if there's an active pin for this phone
        try:
            pin = Pin.objects.get(phone=phone, active=True)
            return pin
        
        # There ain't one, let's make a new one
        except Pin.DoesNotExist:
            pass
        
        while True:
            pin_code = random.randint(1000, 9999)
            try:
                # phone/pin_code is unique, so come up with a unique pair
                pin = Pin.objects.get(phone=phone, pin_code=pin_code)
            except Pin.DoesNotExist:
                try:
                    # We also don't want to generate any pin codes that are already active!
                    # (note there is a race condition here)
                    pin = Pin.objects.get(active=True, pin_code=pin_code)
                except Pin.DoesNotExist:
                    pin = Pin.objects.create(
                        phone=phone,
                        pin_code=pin_code
                    )
                    return pin
    
    @staticmethod
    def resolve_pin(pin_code, computer):
        # TODO: deactivate old pins first
        
        try:
            pin = Pin.objects.get(pin_code=pin_code, active=True)
        except Pin.DoesNotExist:
            return False
        pin.active = False
        pin.computer = computer
        pin.matched = datetime.datetime.now()
        pin.save()
        
        # get_or_create() in case a link for these two already exists
        Link.objects.get_or_create(phone=pin.phone, computer=computer)
        
        return True
