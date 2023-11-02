# android-utilities
Elemental utilities for Android application.

## Callback

  Registering a single callback which will be revoked when the lifecycle owner is destroyed or dispose() method is invoked explicitly.

## Listeners

  Registering multiple callbacks which will be revoked when the lifecycle owner is destroyed or dispose() method is invoked explicitly.

## UtLog

  This is an internal logging system to write logs with class and method name.
  You can instantiate UtLog to use this logging system.
  Though it writes logs to LogCat by default, if you want to use other logging system, implement IUtVaLogger interface and put it to the UtLoggerInstance.externalLogger property.

## Chronos

## IDisposable

### DisposableObserver
### DisposableFlowObserver
### Disposer

## Event
### FlowableEvent
### SuspendableEvent
### 

