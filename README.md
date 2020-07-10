# Google Firebase Cloud Messaging Cordova Push Plugin
> Extremely easy plug&play push notification plugin for Cordova applications with Google Firebase FCM.

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![GitHub issues](https://img.shields.io/github/issues/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound.svg)](https://github.com/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound/issues)
[![GitHub forks](https://img.shields.io/github/forks/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound.svg)](https://github.com/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound/network)
[![GitHub stars](https://img.shields.io/github/stars/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound.svg)](https://github.com/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound/stargazers)
[![Known Vulnerabilities](https://snyk.io/test/github/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound/badge.svg?targetFile=package.json)](https://snyk.io/test/github/binaryops-wiebo/cordova-plugin-fcm-with-dependecy-updated-alarmsound?targetFile=package.json)

[How it works](#how-it-works) | [Installation](#installation) | [Push Payload Configuration](#push-payload-configuration) |  [Features](#features) | [Example Apps](#example-apps) | [Companion Plugins](#companion-plugins) | [Changelog](#changelog) | [Authorship](#authorship)

## Forked from 

https://github.com/andrehtissot/cordova-plugin-fcm-with-dependecy-updated

Refer to his README for up to date instructions on installation and usage

## Alarm functionality

This fork addresses the need to receive an URGENT notification, regardlesss of the cordova application state and start sounding an alarm.

This only works on Android, and only when the Firebase Cloud message is a HIGH priority DATA message. In that case, the plugin has the ability
to start an AlarmService on the device that plays the alarm sound in a continuous loop until the notification is tapped.