# handsaw

A tool for generating i18n strings for multiple platforms.

## Usage
```shell
> ./run.sh
Usage: mi18n [OPTIONS] COMMAND [ARGS]...

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
|mobile|key1|value1|value1|value1|value1|
|android|key2|value2|value2|value2|value2|
|ios|key3|value3|value3|value3|value3|
|mobile|key4|value4|value4|value4|value4|

run command below to generate i18n strings:
```shell
./run.sh gen -i ~/Downloads/client.xlsx -o ~/Downloads/
```

it will generate following files:
- output
    - Android
        - values-en
          - strings.xml
        - values-zh
          - strings.xml
        - values-ja
          - strings.xml
        - values-ms
          - strings.xml
    - iOS
      - en.lproj
        - Localizable.strings
      - zh.lproj
        - Localizable.strings
      - ja.lproj
        - Localizable.strings
      - ms.lproj
        - Localizable.strings

### Read from Android platform

run command below to read i18n strings from Android platform and generate a xlsx file:
```shell
./run.sh read -i ~/android-app/app/src/main/res/ -o ~/Downloads
```
it will generate a client.xlsx file.
