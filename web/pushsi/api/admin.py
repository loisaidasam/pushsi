
# Library
from django.contrib import admin

# Project
from api.models import Phone, Computer, Link, Pin

admin.site.register(Phone)
admin.site.register(Computer)
admin.site.register(Link)
admin.site.register(Pin)
