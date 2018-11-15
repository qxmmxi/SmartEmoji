# SmartEmoji
   Applicable scene: There are scenes that need to input expressions, pictures, and text content, such as: comment component, message input component.
   
## This component supports the features:
### 1.Emoji expression 
      expression pack uses twitter open source emoticon(https://apps.timwhitlock.info/emoji/tables/unicode).
### 2.Custom emoticons
      The configuration file can refer to the configuration information in assests.
### 3.Custom panel（Photo, photo album selection）
      The phone I used during development is Honor V10. In this part, you may need to measure the model adaptation problem.
    
## Emoticon loading logic:
###
   1.The assets are the default expressions; they are read by the configuration information inside.
###
   2.When the application is initialized, it will request configuration information from the server (refer to tabconfigs.json in the assets). If there is an updated expression, it will download the emoticon package and synchronize the local configuration (the emoticon content refers to the assets file).
   
## Other:
   Please refer to BaseBottomBarActivity for specific usage. 
   Please leave a message if you have any questions.

## Project operation effect:
![](https://github.com/qxmmxi/SmartEmoji/blob/master/screenshots/screenshots.png?raw=false)
