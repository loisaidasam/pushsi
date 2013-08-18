# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'Phone'
        db.create_table('api_phone', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('phone_uuid', self.gf('django.db.models.fields.CharField')(unique=True, max_length=255)),
            ('phone_type', self.gf('django.db.models.fields.IntegerField')()),
            ('created', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
        ))
        db.send_create_signal('api', ['Phone'])

        # Adding model 'PushProfileAndroid'
        db.create_table('api_pushprofileandroid', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('phone', self.gf('django.db.models.fields.related.OneToOneField')(to=orm['api.Phone'], unique=True)),
            ('c2dm_token', self.gf('django.db.models.fields.CharField')(max_length=1024, null=True, blank=True)),
            ('created', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
            ('updated', self.gf('django.db.models.fields.DateTimeField')(auto_now=True, blank=True)),
        ))
        db.send_create_signal('api', ['PushProfileAndroid'])

        # Adding model 'PushAlert'
        db.create_table('api_pushalert', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('phone', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['api.Phone'])),
            ('message', self.gf('django.db.models.fields.CharField')(max_length=255)),
            ('external_id', self.gf('django.db.models.fields.CharField')(max_length=255)),
            ('created', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
        ))
        db.send_create_signal('api', ['PushAlert'])

        # Adding model 'Computer'
        db.create_table('api_computer', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('hash', self.gf('django.db.models.fields.CharField')(unique=True, max_length=32)),
            ('created', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
        ))
        db.send_create_signal('api', ['Computer'])

        # Adding model 'Link'
        db.create_table('api_link', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('phone', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['api.Phone'])),
            ('computer', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['api.Computer'])),
        ))
        db.send_create_signal('api', ['Link'])

        # Adding unique constraint on 'Link', fields ['phone', 'computer']
        db.create_unique('api_link', ['phone_id', 'computer_id'])

        # Adding model 'Pin'
        db.create_table('api_pin', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('pin_code', self.gf('django.db.models.fields.CharField')(max_length=4)),
            ('active', self.gf('django.db.models.fields.BooleanField')(default=True)),
            ('phone', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['api.Phone'])),
            ('computer', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['api.Computer'], null=True, blank=True)),
            ('created', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
            ('matched', self.gf('django.db.models.fields.DateTimeField')(null=True, blank=True)),
        ))
        db.send_create_signal('api', ['Pin'])

        # Adding unique constraint on 'Pin', fields ['phone', 'pin_code']
        db.create_unique('api_pin', ['phone_id', 'pin_code'])


    def backwards(self, orm):
        # Removing unique constraint on 'Pin', fields ['phone', 'pin_code']
        db.delete_unique('api_pin', ['phone_id', 'pin_code'])

        # Removing unique constraint on 'Link', fields ['phone', 'computer']
        db.delete_unique('api_link', ['phone_id', 'computer_id'])

        # Deleting model 'Phone'
        db.delete_table('api_phone')

        # Deleting model 'PushProfileAndroid'
        db.delete_table('api_pushprofileandroid')

        # Deleting model 'PushAlert'
        db.delete_table('api_pushalert')

        # Deleting model 'Computer'
        db.delete_table('api_computer')

        # Deleting model 'Link'
        db.delete_table('api_link')

        # Deleting model 'Pin'
        db.delete_table('api_pin')


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
            'message': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
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