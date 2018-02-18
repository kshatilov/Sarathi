# Sarathi

Experimental app for configuration of the traffic offloading.

## Concept

Sarathi application connects to configuration channel using MQQT protocol.
Configuration chanel provides settings, that could be simplified to the list of pairs <Application name, Proxy server parameters>.
Each entry of this list is then represented as a tumbler which in theory should enable/disable traffic offloading for certain app (e.g. YouTube).

Additionally, Sarathi app provides service for sending traffic data and means to configure and observe this service.

## Overview

Sarathi application provides three screens:

1. <b>Settings</b> 

    Allows to configure both configuration subscription channel and metadata publishing channel. 
    Configuration includes server URI, client's ID and topic name. 
     
2. <b>Applications List</b>
    
    List of supported applications is converted to the list of tumblers each of which enables/disables traffic metadata sending for specific app.
    
3. <b>Observer</b>

    Provides a log view for Sarathi service. Sarathi application observes all broadcasted events emitted by service each time it is being triggered.
    
## Points of extension

As far as this application is supposed to be only a small part of traffic's metadata offloading ecosystem, it includes following points of integration:

* <b>Middle boxes protocol</b> 
    
    <i> me/shatilov/symlab/sarathi/model/MiddleBoxModel.java</i> 
        
    This class supposed to be refactored in order to meet requirement and data format for configuration protocol.
    It will also require to adjust (de)serialization and displaying procedures.
    
* <b>Enabling/disabling metadata offloading</b>
    
    <i>me/shatilov/symlab/sarathi/adapters/AppListArrayAdapter.java:49</i>
    
    This is the place where on/off event is observed. With full access to middle box configuration data, handler should call external app (which is in general should be a traffic interceptor for specific app).
    
* <b>Sarathi service interaction</b> 
    
    Setting screen provides an extensive configuration for traffic's metadata publishing mqqt connection.
    Ones "Reconnect" button is trigger newly entered settings are being applied to service configuration.
    
    <i>me/shatilov/symlab/sarathi/activities/SarathiMainActivity.java:217</i>
    
    Example of binding activity to the Sarathi Service.
    
    <i>me/shatilov/symlab/sarathi/activities/SarathiMainActivity.java:201</i>

    Example of triggering binded service. 
    