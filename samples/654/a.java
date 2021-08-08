import java.io.FileOutputStream;

class WaveFileManager {
    /**
     * Save the wave file
     * 
     * @param filename
     *            filename to be saved
     *            
     * @see	Wave file saved
     */
    public void saveWaveAsFile(String filename) {

	WaveHeader waveHeader = wave.getWaveHeader();

	int byteRate = waveHeader.getByteRate();
	int audioFormat = waveHeader.getAudioFormat();
	int sampleRate = waveHeader.getSampleRate();
	int bitsPerSample = waveHeader.getBitsPerSample();
	int channels = waveHeader.getChannels();
	long chunkSize = waveHeader.getChunkSize();
	long subChunk1Size = waveHeader.getSubChunk1Size();
	long subChunk2Size = waveHeader.getSubChunk2Size();
	int blockAlign = waveHeader.getBlockAlign();

	try {
	    FileOutputStream fos = new FileOutputStream(filename);
	    fos.write(WaveHeader.RIFF_HEADER.getBytes());
	    // little endian
	    fos.write(new byte[] { (byte) (chunkSize), (byte) (chunkSize &gt;&gt; 8), (byte) (chunkSize &gt;&gt; 16),
		    (byte) (chunkSize &gt;&gt; 24) });
	    fos.write(WaveHeader.WAVE_HEADER.getBytes());
	    fos.write(WaveHeader.FMT_HEADER.getBytes());
	    fos.write(new byte[] { (byte) (subChunk1Size), (byte) (subChunk1Size &gt;&gt; 8), (byte) (subChunk1Size &gt;&gt; 16),
		    (byte) (subChunk1Size &gt;&gt; 24) });
	    fos.write(new byte[] { (byte) (audioFormat), (byte) (audioFormat &gt;&gt; 8) });
	    fos.write(new byte[] { (byte) (channels), (byte) (channels &gt;&gt; 8) });
	    fos.write(new byte[] { (byte) (sampleRate), (byte) (sampleRate &gt;&gt; 8), (byte) (sampleRate &gt;&gt; 16),
		    (byte) (sampleRate &gt;&gt; 24) });
	    fos.write(new byte[] { (byte) (byteRate), (byte) (byteRate &gt;&gt; 8), (byte) (byteRate &gt;&gt; 16),
		    (byte) (byteRate &gt;&gt; 24) });
	    fos.write(new byte[] { (byte) (blockAlign), (byte) (blockAlign &gt;&gt; 8) });
	    fos.write(new byte[] { (byte) (bitsPerSample), (byte) (bitsPerSample &gt;&gt; 8) });
	    fos.write(WaveHeader.DATA_HEADER.getBytes());
	    fos.write(new byte[] { (byte) (subChunk2Size), (byte) (subChunk2Size &gt;&gt; 8), (byte) (subChunk2Size &gt;&gt; 16),
		    (byte) (subChunk2Size &gt;&gt; 24) });
	    fos.write(wave.getBytes());
	    fos.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private Wave wave;

}

