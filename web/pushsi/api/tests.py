"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.
"""

import hashlib
import json

from django.test import TestCase
from django.test.client import Client

class TestFlow(TestCase):
	
	def test_flow(self):
		c = Client()
		
		phone_uuid = 'abcde-12345-foobar'
		phone_type = 'Android'
		c2dm_token = 'token-blah-blah-foo-29-bar'
		
		# /api/phone/register
		params = {
			'phone_uuid': phone_uuid,
			'phone_type': phone_type,
		}
		response = c.post('/api/phone/register', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict, {'status': True})
		
		# /api/phone/c2dm
		params = {
			'phone_uuid': phone_uuid, 
			'c2dm_token': c2dm_token,
		}
		response = c.post('/api/phone/c2dm', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict, {'status': True})
		
		# /api/phone/status
		params = {
			'phone_uuid': phone_uuid,
		}
		response = c.get('/api/phone/status', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict['computers'], [])
		
		# /api/pin/request
		params = {
			'phone_uuid': phone_uuid,
		}
		response = c.post('/api/pin/request', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		pin_code = response_dict.get('pin_code')
		self.assertTrue(pin_code is not None)
		
		# /api/pin/status
		params = {
			'phone_uuid': phone_uuid,
			'pin_code': pin_code,
		}
		response = c.get('/api/pin/status', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict['resolved'], False)
		
		# /api/computer/register
		response = c.post('/api/computer/register', {})
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertIn('hash', response_dict.keys())
		hash = response_dict['hash']
		
		# /api/computer/status
		params = {
			'hash': hash,
		}
		response = c.get('/api/computer/status', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict['phones'], [])
		
		# /api/pin/resolve
		params = {
			'hash': hash,
			'pin_code': pin_code,
		}
		response = c.post('/api/pin/resolve', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict, {'status': True})
		
		# /api/pin/status
		params = {
			'phone_uuid': phone_uuid,
			'pin_code': pin_code,
		}
		response = c.get('/api/pin/status', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict['resolved'], True)
		
		# /api/computer/status
		params = {
			'hash': hash,
		}
		response = c.get('/api/computer/status', params)
		response_dict = json.loads(response.content)
		self.assertEqual(response.status_code, 200, response_dict.get('error'))
		self.assertEqual(response_dict['phones'][0]['phone_uuid'], phone_uuid)
		