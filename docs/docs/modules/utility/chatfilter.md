---
tags:
  - Legit
---

# **ChatFilter**

#### Filters out specific chat messages.

Command: `/meowfilter`

---

## Filter name

The name of your currently selected filter.

!!! Note

    You do not need to include the file extension.

## Open folder

Opens the folder with chat filters.

## Reload

Reloads currently selected chat filter.

## Usage

1. Open filter folder, can be done with `/meowfilter folder` in-game.

2. Add your filter.

3. Type the filter name in the module text box, or use `/meowfilter select <name>` command.

4. Reload, either with the button in the module or with `/meowfilter reload`.

## Syntax

In order for **ChatFilter** to know which messages should be filtered, you need to use the operators shown below.

The operators need to be **in front** of the text you want to filter, with no spaces.

You can also put `!` in front of an operator, if so it is reversed. As an example `EQUALS` becomes `DOES NOT EQUAL`. These will all be documented below.

!!! Info

    Filters should be .txt files. Each line is handled separately, and text is case-insensitive.

!!! Tip

    You can use the `default.txt` filter as a reference.

### Comments

---

`#`: Comment

Any text after a comment will be ignored, a comment has to be at the start of a line. You can use comments to temporarily disable filter rules without having to delete them, or to document anything that is necessary, which is good practise if you plan to share your filter.

!!! Example

    ``` txt
    # This is a comment
    ```

### Allowed server

---

`-`: Allowed server

You can limit your filter to only work on a specific server or gamemode, this is based on scoreboard. If the scoreboard contains your selected text, the filter will work, otherwise it won't do anything.

If you want it to work everywhere, you can leave this out or put it as `-all`.

!!! Example

    ``` txt
    -hypixel
    ```
  
### Chain conditions

---

`&`: Combine

You can combine conditions, as long as they are on the same line.

!!! Example

    ``` txt
    <Staff have banned an additional & >in the last 7 days.
    ```

### Contains text

---

`?`: Contains

`!?`: Does not contain

!!! Example

    ``` txt
    ?Watchdog has banned
    ```

### Equals text

---

`=`: Equals

`!=`: Does not equal

!!! Example

    ``` txt
    =You are AFK. Move around to return from AFK.
    ```

### Starts with text

---

`<`: Starts with

`!<`: Does not start with

!!! Example

    ``` txt
    <Teaming is not allowed
    ```

### Ends with text

---

`>`: Ends with

`!>`: Does not end with

!!! Example

    ``` txt
    >'s Network Booster)
    ```