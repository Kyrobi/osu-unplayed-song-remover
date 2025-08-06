# Credit
I used this project's code for reading the proprietary
database format: https://github.com/jaasonw/osu-db-tools

# Description
This program allows you easily delete all unplayed songs from your osu's Songs
folder. 

It uses your local scores to determine if you've played a map. These local scores
are stored inside the `scores.db` file inside your osu folder.

Since it's using your local scores, you must have actually played the map. Simply
clicking on a map and marking it as "Mark as played" does not work. If you have
lots of songs you want to keep, as of this moment, the only way is to actually
play it. I recommend using NoFail + DoubleTime to quickly skipping through
each songs and getting a score on the map (even if the score is 0).

# Usage
You can compile the code yourself or grab the Java program from [here](https://github.com/Kyrobi/osu-unplayed-song-remover/releases/tag/release).
After you download it, you can run the program through the terminal with

`java -jar <file name> <path to the osu folder>`

So a complete example would look like this:
`java -jar osu-unplayed-song-remover.jar C:\Users\Kyrobi\AppData\Local\osu!`

This folder should contain a folder called `Songs` and a file called `scores.db`


(I'm just rambling at this point, but it could be useful info for others that
are curious)

# Why
A while back, I downloaded several huge beatmap packs totalling 23k+ songs.
Along with that, it took up around 130GB or my storage. I quickly realized 
that I do not care and will never play the vast vast majority of the songs.

I use Dropbox to sync my important files so that I can backup my files
and easily restore my files when I reinstalled Windows. One of the files
that I care about is my osu file, including all the data inside it.

However, with several small files inside the songs folder 
(music, pngs, sounds, etc), it took absolutely ages to upload the files
and restore them. It also filled my osu client with tons of songs that
I have to go through to even find my songs I care about.

Inside of osu, you can use the search feature `unplayed=` to show all
the songs that you have not played. However, there were some issues.

1. Marking the song as played is bugged, so it will still show up in
unplayed even if you mark it as played. So if you have a bunch of songs
in your collection that you eventually want to pass but you want to keep,
marking them as unplayed won't work.

2. Deleting files through this method takes forever. If I try to delete
more than a couple hundred files at a time, the client will crash. So
I had to delete 200 files at a time (out of 23k files) with
`unplayed= star<5.12` for example.

3. The songs that are deleted with `Delete all visible beatmaps`, it
doesn't actually delete the songs folder, only the beatmap inside the
folder. This means that it'll delete the actual map, but keep all the
other stuff like the music, pngs, sprites, sounds, etc.

So after a while of deleting songs through the `unplayed=` method, I only
reduced the file size from ~130GB to ~111GB, which is not... very good.
With this program, it will actually delete the folders. I managed to
reduce the size of the song folder from 111GB to 11GB.

# Others
I'm sure there are more efficient ways to do this, but this was the first
method that came into my mind, so I stuck with it.

osu stores data like your scores inside a proprietary format.
Since osu stores the songs with their scores locally (via md5 hash),
we can simply read the songs folder, calculate the md5 hash of the
songs, and if the hash is not in the scores, we delete the map.

If even one difficulty is in the hash, we keep the entire folder.
If none of the difficulties are in the hash database, we delete
the entire folder.
