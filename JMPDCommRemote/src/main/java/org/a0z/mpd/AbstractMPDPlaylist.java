package org.a0z.mpd;

import org.a0z.mpd.event.AbstractStatusChangeListener;
import org.a0z.mpd.exception.MPDException;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * RemoteMPD
 * Created by facetoe on 9/02/14.
 */


public abstract class AbstractMPDPlaylist extends AbstractStatusChangeListener {
    protected MusicList list;
    protected int lastPlaylistVersion;
    public interface PlaylistUpdateListener {
        void playlistUpdated();
    }

    protected PlaylistUpdateListener updateListener;

    public void setPlaylistUpdateListener(PlaylistUpdateListener listener) {
        updateListener = listener;
    }
    /**
     * Adds a <code>Collection</code> of <code>Music</code> to playlist.
     *
     * @param c <code>Collection</code> of <code>Music</code> to be added to playlist.
     * @throws org.a0z.mpd.exception.MPDServerException if an error occur while contacting server.
     * @see Music
     */
    public abstract void addAll(Collection<Music> c) throws MPDException;

    /**
     * Adds a music to playlist.
     *
     * @param entry music/directory/playlist to be added.
     * @throws org.a0z.mpd.exception.MPDException if an error occur while contacting server.
     */
    public abstract void add(FilesystemTreeEntry entry) throws MPDException;

    /**
     * Adds a stream to playlist.
     *
     * @param url streams's URL
     * @throws org.a0z.mpd.exception.MPDException
     */
    public abstract void add(URL url) throws MPDException;

    /**
     * Clears playlist content.
     *
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void clear() throws MPDException;

    /**
     * Retrieves all songs as an <code>List</code> of <code>Music</code>.
     *
     * @return all songs as an <code>List</code> of <code>Music</code>.
     * @see Music
     */
    public abstract List<Music> getMusicList();

    /**
     * Retrieves music at position index in playlist. Operates on local copy of playlist, may not reflect server's current playlist.
     *
     * @param index position.
     * @return music at position index.
     */
    public abstract Music getByIndex(int index);

    /**
     * Load playlist file.
     *
     * @param file playlist filename without .m3u extension.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void load(String file) throws MPDException;

    /**
     * Moves song at position <code>from</code> to position <code>to</code>.
     *
     * @param from current position of the song to be moved.
     * @param to   target position of the song to be moved.
     * @throws MPDException if an error occur while contacting server.
     * @see #move(int, int)
     */
    public abstract void moveByPosition(int from, int to) throws MPDException;

    /**
     * Moves song with specified id to position <code>to</code>.
     *
     * @param songId Id of the song to be moved.
     * @param to     target position of the song to be moved.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void move(int songId, int to) throws MPDException;

    @Override
    public abstract void playlistChanged(MPDStatus mpdStatus, int oldPlaylistVersion);

    /**
     * This command works differently than in JMPDComm. If the cached playlistVersion doesn't match
     * the latest one then a request is sent to the server asking for updates. The server responds
     * with a List of changes, which are then updated in the updatePlaylist() method.
     */
    protected abstract int refresh() throws MPDException;


    /**
     * Remove playlist entry at position index.
     *
     * @param position position of the entry to be removed.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void removeByIndex(int position) throws MPDException;

    /**
     * Removes entries from playlist.
     *
     * @param songs entries positions.
     * @throws MPDException if an error occur while contacting server.
     * @see #removeById(int[])
     */
    public abstract void removeByIndex(int[] songs) throws MPDException;

    /**
     * Remove playlist entry with ID songId
     *
     * @param songId id of the entry to be removed.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void removeById(int songId) throws MPDException;

    /**
     * Removes entries from playlist.
     *
     * @param songIds entries IDs.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void removeById(int[] songIds) throws MPDException;

    /**
     * Removes album of given ID from playlist.
     *
     * @param songId entries positions.
     * @throws MPDException if an error occur while contacting server.
     * @see #removeById(int[])
     */
    public abstract void removeAlbumById(int songId) throws MPDException;

    /**
     * Removes playlist file.
     *
     * @param file playlist filename without .m3u extension.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void removePlaylist(String file) throws MPDException;

    /**
     * Save playlist file.
     *
     * @param file playlist filename without .m3u extension.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void savePlaylist(String file) throws MPDException;

    /**
     * Shuffles playlist content.
     *
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void shuffle() throws MPDException;

    /**
     * Retrieves playlist size. Operates on local copy of playlist, may not reflect server's current playlist. You may call refresh() before
     * calling size().
     *
     * @return playlist size.
     */
    public abstract int size();

    /**
     * Swap positions of song1 and song2.
     *
     * @param song1 position of song1 in playlist.
     * @param song2 position of song2 in playlist
     * @throws MPDException if an error occur while contacting server.
     * @see #swap(int, int)
     */
    public abstract void swapByPosition(int song1, int song2) throws MPDException;

    /**
     * Swap positions of song1 and song2.
     *
     * @param song1Id id of song1 in playlist.
     * @param song2Id id of song2 in playlist.
     * @throws MPDException if an error occur while contacting server.
     */
    public abstract void swap(int song1Id, int song2Id) throws MPDException;

    /**
     * Retrieves a string representation of the object.
     *
     * @return a string representation of the object.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Music m : list.getMusic()) {
            sb.append(m.toString()).append("\n");
        }
        return sb.toString();
    }
}