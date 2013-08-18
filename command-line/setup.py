#!/usr/bin/env python
# coding: utf-8

from distutils.core import setup

setup(
    name='pushsi',
    version='0.2',
    description="A simple library for sending push notifications to your phone, particularly via command line",
    author="@loisaidasam",
    scripts=['pushsi'],
    requires=['requests'],
)
