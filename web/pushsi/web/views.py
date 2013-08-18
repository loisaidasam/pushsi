
# General
import logging

# Library
from django.conf import settings
from django.shortcuts import render_to_response
from django.template import RequestContext
from django.views.decorators.csrf import csrf_protect

# Project
from api.models import Phone, Computer, PushAlert

# Environment
logger = logging.getLogger(__name__)

@csrf_protect
def index(request):
	context = {
		'phones': Phone.objects.all().count(),
		'computers': Computer.objects.all().count(),
		'pushes': PushAlert.objects.all().count(),
	}
	return render_to_response('index.html', context, RequestContext(request))
