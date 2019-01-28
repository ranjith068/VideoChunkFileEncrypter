VideoChunkEncryption-Android
=================

VideoChunkEncryption will convert video files to different pieces and encrypt the files so that no other player will be able to play the video. Only the Player in our app can able to play the encrypted file pieces like Youtube,Hotstar playing there offline videos.

## FFMPEG
    
   ffmpeg is used to split the video files.

## Cipher AES 
    
    Cipher AES is been used to encrypt the chunk files

## Exoplayer
 
   Exo player is used to  play the decrypt the chunk files and play in real time.Some basic customization has added to exo player 

##VideoPreview

[![Video](https://img.youtube.com/vi/PP7Gj3X2hJ8/0.jpg)](https://www.youtube.com/watch?v=PP7Gj3X2hJ8)


## Why this project?

I search many place for a solution like this but no where a sample to start with, so i made a basic idea and added this project so that it will give some idea for developer who is about to start something like this.The basic aim of this project is to give developers some help in chunk players, like Youtube,Hotstar etc playing there offline videos, i wont say they are using this, but just this will give a basic solution, you can use any decryption encryption algorithm to make the pieces safe.Hope this will help some one.


## library used in this project

**1** ([ffmpeg-android-java](https://github.com/WritingMinds/ffmpeg-android-java)) 

**2** ([Exoplayer](https://github.com/google/ExoPlayer)) 

**3** ([android-file-chooser](https://github.com/hedzr/android-file-chooser)) 

## To Do

Compress chunk files
Apk size reducing





## License

    The MIT License (MIT)

    Copyright (c) 2018 Ramesh M Nair
 
     Permission is hereby granted, free of charge, to any person obtaining a copy
     of this software and associated documentation files (the "Software"), to deal
     in the Software without restriction, including without limitation the rights
     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
     copies of the Software, and to permit persons to whom the Software is
     furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.



