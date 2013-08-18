
import logging
import re
import urllib
import urllib2

from django.conf import settings

from api.models_static import PHONE_TYPE_ANDROID

logger = logging.getLogger(__name__)


class PushException(Exception):
    pass


def _send_android_push(push_message, c2dm_token):
    url = 'https://android.apis.google.com/c2dm/send'
    values = {
        'registration_id' : c2dm_token,
        'collapse_key': 'pushsi',
        'data.message': push_message,
    }
    headers = { 'Authorization' : "GoogleLogin auth=%s" % settings.GOOGLE_AUTH_TOKEN }
    
    data = urllib.urlencode(values)
    req = urllib2.Request(url, data, headers)
    
    logging.info("Sending android push with google auth_token=%s and c2dm_token=%s" % (settings.GOOGLE_AUTH_TOKEN, c2dm_token))
    
    try:
        response = urllib2.urlopen(req)
        result = response.read()
    except IOError:
        raise PushException("IOError trying to send push message: %s (c2dm_token=%s)" % (
            push_message, c2dm_token
        ))
    except Exception, e:
        raise PushException("Exception: '%s' trying to send push message: %s (c2dm_token=%s)" % (
            str(e), push_message, c2dm_token
        ))
    
    response_code = response.getcode()
    
    logger.info("Response code=%s body:\n%s" % (response_code, result))
    
    if response_code == 200:
        m = re.match(r"id=(.+)", result)
        if not m:
            raise PushException("Unable to parse id from response")
    
        return m.group(1)
    
    if response_code == 503:
        raise PushException("503: Server temporarily unavailable - retry later (see https://developers.google.com/android/c2dm/)")

    if response_code == 401:
        raise PushException("401: Invalid ClientLogin AUTH_TOKEN (see https://developers.google.com/android/c2dm/)")
        # TODO: definitely email me and let me know this one!
    
    raise PushException("Strange response code: %s" % response_code)


def send_push(phone_type, push_message, **kwargs):
    if phone_type == PHONE_TYPE_ANDROID:
        c2dm_token = kwargs.get('c2dm_token')
        if not c2dm_token:
            raise PushException("No c2dm_token passed to send_push()")
        return _send_android_push(push_message, c2dm_token)
    
    raise PushException("Invalid phone_type %s specified" % phone_type)
