# XyMusic

XyMusic manages music-library connections and playlist workflows across mobile
and desktop clients. This glossary keeps playlist file language separate from
server-side playlist changes.

## Language

**Music Favorite Toggle**:
A single-song favorite or unfavorite action initiated from a music row, menu,
snackbar, or player control. It is distinct from album favorites, artist
favorites, and batch operations over selected songs.
_Avoid_: album favorite, artist favorite, playlist membership, batch unfavorite

**Playlist**:
A server-side music collection that can contain many songs and can be imported
from or exported to a playlist file.
_Avoid_: album, local file

**Playlist File**:
A local `.txt` or `.m3u8` file that represents playlist contents for transfer
between XyMusic and the user's filesystem.
_Avoid_: server playlist, export data

**Playlist File Import**:
The act of selecting, reading, and parsing a playlist file before asking the
user whether to import it into a server-side playlist.
_Avoid_: service import, create playlist

**Server Playlist Import**:
The act of applying parsed playlist contents to the connected music service.
This happens after playlist file import succeeds and after user confirmation.
_Avoid_: file parsing, file import

**Playlist File Export**:
The act of generating playlist file contents from a server-side playlist and
writing them to a user-selected local file.
_Avoid_: server sync, playlist backup job

## Example Dialogue

Dev: "When export starts, should the loading message mention the server
playlist or the file?"

Domain expert: "Mention the playlist file. The user chose a `.txt` or `.m3u8`
export, so the message should say which playlist file is being generated or
written."

Dev: "For import, is reading the file the same as importing the playlist?"

Domain expert: "No. Playlist file import reads and parses the local file. Server
playlist import starts only after the user confirms the parsed playlist should
be applied to the connected service."

Dev: "Should a favorite click on an album page be treated as a music favorite
toggle?"

Domain expert: "Only the individual song rows are music favorite toggles. The
album header and artist header favorites are separate concepts, and selected
song batch actions are not the same as one click on one song."
