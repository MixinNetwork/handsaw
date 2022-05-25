# handsaw

A tool for generating i18n strings for multiple platforms.

## Usage

Install on MacOS with:
```
$ curl -L  https://raw.githubusercontent.com/MixinNetwork/handsaw/main/handsaw.rb > handsaw.rb && brew install handsaw.rb && rm handsaw.rb
```

For other platforms, download ZIP from [latest release](https://github.com/MixinNetwork/handsaw/releases/latest) and run: `bin/handsaw` or `bin/handsaw.bat`.

```
$ handsaw --help
Usage: handsaw [OPTIONS] COMMAND [ARGS]...

Options:
  -h, --help  Show this message and exit

Commands:
  gen   Generate i18n strings from other sources, only xlsx file supported for now
  read  Read i18n strings from specify platform
```

### Generate from xlsx file

maintain a xlsx file with the following structure:

|platform|keys|en|zh|ja|ms|
| --- | --- | --- | --- | --- | --- |
|Android,iOS,Desktop|key1|value1|value1|value1|value1|
|Android|key2|value2|value2|value2|value2|
|iOS|key3|value3|value3|value3|value3|
|Android,ios,Desktop|key4|value4|value4|value4|value4|

run command below to generate i18n strings:
```
$ handsaw gen -i ~/Downloads/client.xlsx -o ~/Downloads/
```

it will generate following files:
- output
    - Android
        - values-en
          - strings.xml
        - ...
        - values-zh
          - strings.xml
        - values-ja
          - strings.xml
        - values-ms
          - strings.xml
    - iOS
      - en.lproj
        - Localizable.strings
      - ...
      - zh.lproj
        - Localizable.strings
      - ja.lproj
        - Localizable.strings
      - ms.lproj
        - Localizable.strings
    - Flutter
      - intl_en.arb
      - intl_ja.arb
      - intl_zh.arb

#### placeholders
general platform placeholders use format like `%1$s` `%2$d`, it will generate platform specific placeholders, for example: `%@` `%d` for iOS etc.
special platform can use its own placeholders, for example: iOS use `%@` `%d`, Android use `%1$s` `%2$d`, etc.

#### iOS key-type
iOS platform support parameter key-type, default value is 0, which means use the xlsx key as the key, 1 means use the English column as the key.
```
$ handsaw gen -k 1
```

#### plural case
if you want generate plural format like:
```xml
<plurals name="number_of_day">
    <item quantity="other">%1$d days remaining</item>
    <item quantity="one">one day remaining</item>
</plurals>
```
you need name the key as `number_of_day.count` and `number_of_day`

e,g:

|platform|keys|en|zh|
| --- | --- | --- | --- |
|Android,iOS,Desktop|continue.count|Continue(%1$s)|继续(%1$s)|
|Android,iOS,Desktop|number_of_day.count|%1$d days remaining|%1$d 天剩余|
|Android,iOS,Desktop|number_of_day|one day remaining||
|Android,iOS,Desktop|participant_count|%1$d participants|%1$d 名成员|
|Android,iOS,Desktop|participant_count.count|%1$d participants|%1$d 名成员|

will generate following strings for Android:
```xml
<plurals name="continue" tools:ignore="UnusedQuantity">
  <item quantity="other">Continue(%1$s)</item>
</plurals>
<plurals name="number_of_day">
  <item quantity="other">%1$d days remaining</item>
  <item quantity="one">one day remaining</item>
</plurals>
<plurals name="participant_count">
  <item quantity="one">%1$d participant</item>
  <item quantity="other">%1$d participants</item>
</plurals>

<plurals name="continue" tools:ignore="UnusedQuantity">
  <item quantity="other">继续（%1$s）</item>
</plurals>
<plurals name="number_of_day" tools:ignore="UnusedQuantity">
  <item quantity="other">剩余%1$d天</item>
</plurals>
<plurals name="participant_count">
  <item quantity="one">%1$d 名成员</item>
  <item quantity="other">%1$d 名成员</item>
</plurals>
```

following strings for iOS:
```
"continue_count" = "Continue(%1$@)";
"number_of_day_count" = "%1$@ days remaining";
"number_of_day" = "one day remaining";
"participant_count" = "%1$@ participant";
"participant_count_count" = "%1$@ participants";

"continue_count" = "继续（%1$@）";
"number_of_day_count" = "剩余%1$@天";
"participant_count" = "%1$@ 名成员";
"participant_count_count" = "%1$@ 名成员";
```

with `-k 1` option:
```
"Continue(%@)" = "Continue(%@)";
"Continue(%@)" = "Continue(%@)";
"%d days remaining" = "%d days remaining";
"one day remaining" = "one day remaining";
"%d participant" = "%d participant";
"%d participants" = "%d participants";

"Continue(%@)" = "继续（%@）";
"Continue(%@)" = "继续（%@）";
"%d days remaining" = "剩余%d天";
"%d participant" = "%d 名成员";
"%d participants" = "%d 名成员";
```

following strings for Flutter:
```
{
"continueCount" : "Continue{arg0}",
"numberOfDayCount" : "%1$@ days remaining",
"numberOfDay" : "one day remaining",
"participantCount" : "%1$@ participant",
"participantCountCount" : "%1$@ participants",

"continueCount" : "继续（%1$@）",
"numberOfDayCount" : "剩余%1$@天",
"participantCount" : "%1$@ 名成员",
"participantCountCount" : "%1$@ 名成员",
}
```


### Read from Android platform

run command below to read i18n strings from Android platform and generate a xlsx file:
```
$ handsaw read -i ~/android-app/app/src/main/res/ -o ~/Downloads
```
it will generate a client.xlsx file.
