
from django.core.urlresolvers import reverse
from django.test import TestCase
from django.test.client import Client


class AvailabilityTest(TestCase):
    def setUp(self):
        self.client = Client()

    def test_web(self):
        url = reverse('index')
        response = self.client.get(url)
        self.assertEqual(response.status_code, 200)
