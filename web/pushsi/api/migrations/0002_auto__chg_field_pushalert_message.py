# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):

        # Changing field 'PushAlert.message'
        db.alter_column('api_pushalert', 'message', self.gf('django.db.models.fields.TextField')())

    def backwards(self, orm):

        # Changing field 'PushAlert.message'
        db.alter_column('api_pushalert', 'message', self.gf('django.db.models.fields.CharField')(max_length=255))

    models = {
        'api.computer': {
            'Meta': {'object_name': 'Computer'},
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'hash': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '32'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'phones': ('django.db.models.fields.related.ManyToManyField', [], {'to': "orm['api.Phone']", 'through': "orm['api.Link']", 'symmetrical': 'False'})
        },
        'api.link': {
            'Meta': {'unique_together': "(('phone', 'computer'),)", 'object_name': 'Link'},
            'computer': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['api.Computer']"}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'phone': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['api.Phone']"})
        },
        'api.phone': {
            'Meta': {'object_name': 'Phone'},
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'phone_type': ('django.db.models.fields.IntegerField', [], {}),
            'phone_uuid': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '255'})
        },
        'api.pin': {
            'Meta': {'unique_together': "(('phone', 'pin_code'),)", 'object_name': 'Pin'},
            'active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
            'computer': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['api.Computer']", 'null': 'True', 'blank': 'True'}),
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'matched': ('django.db.models.fields.DateTimeField', [], {'null': 'True', 'blank': 'True'}),
            'phone': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['api.Phone']"}),
            'pin_code': ('django.db.models.fields.CharField', [], {'max_length': '4'})
        },
        'api.pushalert': {
            'Meta': {'object_name': 'PushAlert'},
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'external_id': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'message': ('django.db.models.fields.TextField', [], {}),
            'phone': ('django.db.models.fields.related.ForeignKey', [], {'to': "orm['api.Phone']"})
        },
        'api.pushprofileandroid': {
            'Meta': {'object_name': 'PushProfileAndroid'},
            'c2dm_token': ('django.db.models.fields.CharField', [], {'max_length': '1024', 'null': 'True', 'blank': 'True'}),
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'phone': ('django.db.models.fields.related.OneToOneField', [], {'to': "orm['api.Phone']", 'unique': 'True'}),
            'updated': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'})
        }
    }

    complete_apps = ['api']