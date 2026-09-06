---
tags:
  - Safe
---

# **NickBot**

#### Rolls nicks for you until a good one is found.

!!! Warning

    This is a bot, it is not a legit feature. However all it does is send chat messages, so it should generally be safe.

---

## Delay

Time to wait until rolling another nick.

!!! Tip

    It is recommended that you optimize delay with the ignore limbo kick option, to make it as efficient as possible without losing too much time to limbo kicks.

## Start

Starts the bot when clicked.

## Auto accept good nick

Will automatically accept the nick if you get a good one, otherwise it will just leave the book open for you to manually accept it.

## Ignore limbo kick

If you get kicked to limbo (usually due to spam) this will automatically go back to lobby, and continue rolling nicks.

## List skipped nicks

Sends a chat message for each nick that is skipped.

!!! Tip

    You can do `/meowlog` to open the log if you want to see the full list of skipped nicks.

## Legacy nicks

Searches for legacy nicks.

!!! Note

    These are extremely rare, you should not expect to ever get one however just in-case you should always keep this enabled.

## Four char

Searches for 4 character nicks.

## Max char

Searches for nicks that have the maximum amount of allowed characters (16).

## Plain text

Searches for nicks that only contain text, ignores underscores.

## Special affixes

Searches for nicks with a special prefix or suffix.

!!! Example

    xX_Melon_Xx

## Custom words

Uses your custom word list.

## Commands

`/nickbot` - Shows all NickBot commands.

`/nickbot start` - Starts the NickBot.

`/nickbot add` - Add word to the list.

`/nickbot remove` - Remove word from the list.

`/nickbot list` - Show all words currently in the list.

### List syntax:

`?meow` - _If the nick contains meow._

`=meow` - _If the nick equals meow._

`<meow` - _If the nick starts with meow._

`>meow` - _If the nick ends with meow._

!!! Example

    /nickbot add ?Meow will check if a nick contains the word "meow", and if so flag it as a good nick.
