from django.conf.urls.defaults import *

urlpatterns = patterns('web.views',
    url(r'^$', 'index', name='index'),
)
