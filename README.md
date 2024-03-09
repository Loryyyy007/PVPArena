`Description`

This plugin allows players to create arenas around the Minecraft world setting them with the */arena command*.  
Players in this arenas can fight until death whereas in the outside world they can still damage each other but not die to each other.  
There are lots of customizable options and the plugin aims to be as user-friendly as possible.

Supported Minecraft version: **1.20.1**.

Enjoy!


`Commands`

The comand /arena is the only command of the plugin and it's used mainly to create and manage arenas with an easy, user-friendly sintax. It can be abbrevieted to /arn, and can be used only by players.  

The possible arguments are the following:

  */arena* -> shows the arena you are currently in.  
  */arena wand* -> gives the tool to select regions for the arenas.  
  */arena save \<arenaName>* -> saves an arena as the selected region.  
  */arena changeName \<oldArenaName> \<newArenaName>* -> changes the name of an arena.  
  */arena delete \<arenaName>* -> deletes an arena.  
  */arena pos1* -> sets pos1 to your current location.  
  */arena pos2* -> sets pos2 to your current location.  
  */arena expand \<amount> (\<direction>)* -> expands the selected region in a certain direction.  
  */arena reduce \<amount> (\<direction>)* -> reduces the selected region in a certain direction.  
  */arena move \<amount> (\<direction>)* -> moves the selected region in a certain direction.  
  */arena enable* -> enables the enter/exit arena check.  
  */arena disable* -> disables the enter/exit arena check.  
  */arena enable \<arenaName>* -> enables a certain arena.  
  */arena disable \<arenaName>* -> disables a certain arena.  
  */arena info* -> shows some info of all arenas.  
  */arena info \<arenaName>* -> shows some info of a certain arena.  
  */arena reload* -> reloads the plugin. 
  */arena confReload* -> reloads all configs.  

`Arena Wand`

The Arena Wand is a powerful tool that makes setting regions way easier.  
To get it just use the command /arena wand and it will appear in your inventory.  

The Wand can do 5 different things:
 - Left-click a block to set the pos1 of the region.  
 - Right-click a block to set the pos2 of the region.  
 - Right-click air to expand the region in the direction you are facing by the amount provided in the config.yml.  
 - Shift+right-click air to reduce the region in the direction you are facing by the amount provided in the config.yml.  
 - Drop to move the region in the direction you are facing by the amount provided in the config.yml.  

In addition, the region will only be visible if the Arena Wand is held, so keep that in mind.  

`Permissions`

*arena.set*: allows a player to create and manage arenas  
*arena.enable*: allows a player to enable or disable arenas and the arena join/leave check  
*arena.info*: allows a player to see the info about all arenas  
*arena.which*: allows a player to know in which arena they're in (/arena)  
*arena.configs-reload*: allows a player to reload all the configs of this plugin (/arena confReload)  
*arena.reload*: allows a player to reload this plugin (/arena reload)  
_arena.*_: equivalent to every permission stated above  

`Configuration`

The values below are the default suggested implementations, but they can be changed at any time.  
The plugin needs to be reloaded, or the /arena confReload command need to be run for these to take effect.


```yml

General:
  Arena-enabled-on-creation: true  #toggles the default value of the "enable" option of an arena.
  Out-of-arena-damage: true  #toggles if outside of arenas players can damage each other.
  Out-of-arena-interference: false  #toggles if the players outside of arenas can interfer with those in arenas.
Region:
  Make-corners-precise-on-creation: true  #toggles if regions should have precise corners, that is the most extern part of the block, instead of the center of it.
  Visible: true  #toggles the selected region visibility.
  Visibility-period: 4  #this is the speed in ticks (20th of a second) at which region particles are updated.
  Moving-amount: 1  #this is the amount of blocks by which the region gets moved when using the Arena Wand.  
  Expanding-reducing-amount: 1  #this is the amount of blocks by which the region gets expanded or reduced when using the Arena Wand.
  Particle: null  #this is the particle that will make up the selected region. If an unvalid particle is provided, this will just be VILLAGER_HAPPY
  Particle-max-amount: 15000  #this is the maximum amount of particles that will be displayed for the region. If this limit is exceeded no particle will be displayed.
Task:
  Delay: 10  #this is the delay in ticks (20th of a second) of the arena join/leave task, that is how much time it waits before starting upon activation.
  Period: 10  #this is the speed in ticks (20th of a second) of the arena join/leave task.
  Enabled: true  #enables the arena join/leave task; this can be toggled with /arena enable/disable.
Messages:
  Join-arena: true  #enables the on aena join message.
  Leave-arena: true  #enables the on aena leave message.
  Death-in-arena: true  #enables the on aena death message.

```

`Messages config`

The values below are the default suggested implementations, but they can be changed at any time.  
The plugin needs to be reloaded, or the /arena confReload command need to be run for these to take effect.

```yml

on-arena-join: '&3<player> &ahas joined &6<arena>.'  #sent, if enabled in the config.yml, to all players in the server when a player joins an arena.
on-arena-leave: '&3<player> &ahas left &6<arena>.'  #sent, if enabled in the config.yml, to all players in the server when a player leaves an arena.
on-arena-death: '&6<victim> &chas been killed by &4<killer>.'  #sent, if enabled in the config.yml, to all players in the server when a player dies in an arena.
no-permission: '&cYou don''t have the required permission to execute this command.'  #sent when a player doesn't have the permission to execute a command.
arena-which: '&aYou are currently in &6<arena> arena.'  #sent when a player runs the /arena command and they are in an arena.
not-in-arena: '&cYou are not in an arena.'  #sent when a player runs the /arena command and they are not in an arena.

```

More than one value can be provided for each message as long as they are in a list like in the following example:

```yml

on-arena-join:
  - '&3<player> &ahas joined &6<arena>.'
  - '&3<player> &ahas popped into &6<arena>.'
  - 'Welcome &3<player> &e to the awesome &6<arena> arena.'  

```
Only one of this message will be chosen randomically.  
In the messages some variables can be used: \<player> and \<arena>. 
These variables will be replaced with the actual name of the player/arena needed in the message (they can also not be used). See above to understand where they are used.  
Moreover, to color a message the '&' character has to be used following the Minecraft color codes.
