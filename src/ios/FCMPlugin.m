#include <sys/types.h>
#include <sys/sysctl.h>
#import "AppDelegate+FCMPlugin.h"
#import <UserNotifications/UserNotifications.h>
#import <Cordova/CDV.h>
#import <WebKit/WebKit.h>
#import "FCMPlugin.h"
#import "Firebase.h"

@interface FCMPlugin () {}
@end

@implementation FCMPlugin

static BOOL appInForeground = YES;

static NSString *notificationEventName = @"notification";
static NSString *tokenRefreshCallback = @"tokenRefresh";
static NSString *jsEventBridgeCallbackId;
static FCMPlugin *fcmPluginInstance;

+ (FCMPlugin *)fcmPlugin {
    return fcmPluginInstance;
}

- (void)ready:(CDVInvokedUrlCommand *)command {
    NSLog(@"Cordova view ready");
    fcmPluginInstance = self;
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

// HAS PERMISSION //
- (void)hasPermission:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        [AppDelegate hasPushPermission:^(NSNumber* pushPermission){
            __block CDVPluginResult *commandResult;
            if (pushPermission == nil) {
                NSLog(@"has push permission: unknown");
                commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            } else if ([pushPermission boolValue] == YES) {
                NSLog(@"has push permission: true");
                commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
            } else if ([pushPermission boolValue] == NO) {
                NSLog(@"has push permission: false");
                commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:NO];
            }
            [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
        }];
    }];
}

// START JS EVENT BRIDGE //
- (void)startJsEventBridge:(CDVInvokedUrlCommand *)command {
    NSLog(@"start Js Event Bridge");
    jsEventBridgeCallbackId = command.callbackId;
}

// GET TOKEN //
- (void)getToken:(CDVInvokedUrlCommand *)command {
    NSLog(@"get Token");
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;
        NSString* fcmToken = [AppDelegate getFCMToken];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:fcmToken];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

// GET APNS TOKEN //
- (void)getAPNSToken:(CDVInvokedUrlCommand *)command  {
    NSLog(@"get APNS Token");
    [self.commandDelegate runInBackground:^{
        CDVPluginResult* pluginResult = nil;
        NSString* apnsToken = [AppDelegate getAPNSToken];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:apnsToken];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

// CLEAR ALL NOTIFICATONS //
- (void)clearAllNotifications:(CDVInvokedUrlCommand *)command {
  [self.commandDelegate runInBackground:^{
    NSLog(@"clear all notifications");
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:1];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

// UN/SUBSCRIBE TOPIC //
- (void)subscribeToTopic:(CDVInvokedUrlCommand *)command {
    NSString* topic = [command.arguments objectAtIndex:0];
    NSLog(@"subscribe To Topic %@", topic);
    [self.commandDelegate runInBackground:^{
        if(topic != nil)[[FIRMessaging messaging] subscribeToTopic:topic];
        CDVPluginResult* pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:topic];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)unsubscribeFromTopic:(CDVInvokedUrlCommand *)command {
    NSString* topic = [command.arguments objectAtIndex:0];
    NSLog(@"unsubscribe From Topic %@", topic);
    [self.commandDelegate runInBackground:^{
        if(topic != nil)[[FIRMessaging messaging] unsubscribeFromTopic:topic];
        CDVPluginResult* pluginResult = nil;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:topic];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)requestPushPermission:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSNumber* ios9SupportTimeout = [command argumentAtIndex:0 withDefault:[NSNumber numberWithFloat:10]];
        NSNumber* ios9SupportInterval = [command argumentAtIndex:1 withDefault:[NSNumber numberWithFloat:0.3]];
        NSLog(@"requestPushPermission { ios9SupportTimeout:%@ ios9SupportInterval:%@ }", ios9SupportTimeout, ios9SupportInterval);
        id objects[] = { ios9SupportTimeout, ios9SupportInterval };
        id keys[] = { @"ios9SupportTimeout", @"ios9SupportInterval" };
        NSDictionary* options = [NSDictionary dictionaryWithObjects:objects forKeys:keys count:2];
        [AppDelegate requestPushPermission:^(BOOL pushPermission, NSError* _Nullable error) {
            if(error != nil){
                NSLog(@"push permission request error: %@", error);
                __block CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[error description]];
                [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
                return;
            }
            NSLog(@"push permission request result: %@", pushPermission ? @"Yes" : @"No");
            __block CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:pushPermission];
            [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
        } withOptions:options];
    }];
}

- (void)getInitialPushPayload:(CDVInvokedUrlCommand *)command {
     NSLog(@"getInitialPushPayload");
    [self.commandDelegate runInBackground:^{
        NSData* payload = [AppDelegate getInitialPushPayload];
        if (payload == nil) {
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:nil];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
            return;
        }
        NSDictionary *payloadDictionary = (NSDictionary*) [NSKeyedUnarchiver unarchiveObjectWithData:payload];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:payloadDictionary];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)notifyOfMessage:(NSData *)payload {
    NSLog(@"notifyOfMessage payload: %@", payload);
    NSString* JSONString = [[NSString alloc] initWithBytes:[payload bytes] length:[payload length] encoding:NSUTF8StringEncoding];
    [self dispatchJSEvent:notificationEventName withData:JSONString];
}

- (void)notifyFCMTokenRefresh:(NSString *)token {
    NSLog(@"notifyFCMTokenRefresh token: %@", token);
    NSString* jsToken = [NSString stringWithFormat:@"\"%@\"", token];
    [self dispatchJSEvent:tokenRefreshCallback withData:jsToken];
}

- (void)dispatchJSEvent:(NSString *)eventName withData:(NSString *)jsData {
    if(jsEventBridgeCallbackId == nil) {
        NSLog(@"dispatchJSEvent: Unable to send event due to unreachable bridge context: %@ with %@", eventName, jsData);
        return;
    }
    NSLog(@"dispatchJSEvent: %@ with %@", eventName, jsData);
    NSString* eventDataTemplate = @"[\"%@\",%@]";
    NSString* eventData = [NSString stringWithFormat:eventDataTemplate, eventName, jsData];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:eventData];
    [pluginResult setKeepCallbackAsBool:TRUE];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:jsEventBridgeCallbackId];
}

- (void)appEnterBackground {
    NSLog(@"Set state background");
    appInForeground = NO;
}

- (void)appEnterForeground {
    NSLog(@"Set state foreground");
    NSData* lastPush = [AppDelegate getLastPush];
    if (lastPush != nil) {
        [FCMPlugin.fcmPlugin notifyOfMessage:lastPush];
    }
    appInForeground = YES;
}

@end
