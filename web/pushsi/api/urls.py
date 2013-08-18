from django.conf.urls.defaults import *

urlpatterns = patterns('api.views',
    url(r'^phone/register', 'phone_register', name='phone_register'),
    url(r'^phone/c2dm', 'phone_c2dm', name='phone_c2dm'),
    url(r'^phone/status', 'phone_status', name='phone_status'),
    
    url(r'^pin/request', 'pin_request', name='pin_request'),
    url(r'^pin/resolve', 'pin_resolve', name='pin_resolve'),
    url(r'^pin/status', 'pin_status', name='pin_status'),
    
    url(r'^computer/register', 'computer_register', name='computer_register'),
    url(r'^computer/status', 'computer_status', name='computer_status'),
    
    url(r'^push', 'push', name='push'),
)