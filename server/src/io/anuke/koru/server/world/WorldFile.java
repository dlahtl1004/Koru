package io.anuke.koru.server.world;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.compression.Lzma;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import io.anuke.koru.Koru;
import io.anuke.koru.world.*;
import io.anuke.ucore.util.ColorCodes;

//TODO do something about the chunk writers
public class WorldFile extends WorldLoader{
	public final String filename = "chunk", extension = ".kw";
	private Path file;
	private ConcurrentHashMap<String, Path> files = new ConcurrentHashMap<String, Path>();
	private Kryo kryo; // for reading only
	private ConcurrentHashMap<Long, Chunk> loadedchunks = new ConcurrentHashMap<Long, Chunk>(); // server-side
																								// chunks
	private Generator generator;
	private ChunkWriter[] writers = new ChunkWriter[Runtime.getRuntime().availableProcessors()];
	private Object lock = new Object();
	private boolean debug;
	private boolean compress = false;

	public WorldFile(Path file, Generator generator) {

		if(!Files.isDirectory(file))
			throw new RuntimeException("World file has to be a directory!");

		for(int i = 0; i < writers.length; i++){
			writers[i] = new ChunkWriter();
		}

		kryo = new Kryo();
		kryo.register(Chunk.class);
		kryo.register(Materials.class);
		this.file = file;
		this.generator = generator;

		try{
			Stream<Path> stream = Files.list(file);

			stream.forEach((Path path) -> {
				if(path.toString().endsWith(extension))
					files.put(path.getFileName().toString(), path);
			});

			stream.close();
		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}
		if(files.size() > 0){
			Koru.log("Found " + files.size() + " world chunk" + (files.size() == 1 ? "" : "s") + ".");
		}else{
			Koru.log("Found empty world.");
		}

	}

	public boolean chunkIsSaved(int x, int y){
		return getPath(x, y) != null;
	}

	public void writeChunk(Chunk chunk, int writer){
		Path path = Paths.get(file.toString(), "/" + fileName(chunk.x, chunk.y));
		
		while(!writers[writer].writing()){ // to prevent screwups with multiple threads using writer 0
			writers[writer].writeChunk(chunk, path, compress);
			files.put(path.getFileName().toString(), path);
			return;
		}
		
	}

	/*
	 * public void writeChunk(Chunk chunk){ synchronized (lock){ if(debug)
	 * Koru.log(Text.RED + "BEGIN" + Text.BLUE + " write chunk" + Text.RESET);
	 * Path path = Paths.get(file.toString(), "/" + fileName(chunk.x, chunk.y));
	 * 
	 * long time = TimeUtils.millis();
	 * 
	 * try{ ByteArrayOutputStream stream = new ByteArrayOutputStream(); Output
	 * output = new Output(stream); kryo.writeObject(output, chunk);
	 * output.close();
	 * 
	 * ByteArrayInputStream in = new ByteArrayInputStream(stream.toByteArray());
	 * FileOutputStream file = new FileOutputStream(path.toString());
	 * 
	 * Lzma.compress(in, file);
	 * 
	 * stream.close(); in.close(); file.close();
	 * 
	 * if(debug) Koru.log("Chunk write time elapsed: " +
	 * TimeUtils.timeSinceMillis(time)); }catch(Exception e){
	 * Koru.log("Error writing chunk!"); e.printStackTrace(); }
	 * files.put(path.getFileName().toString(), path); if(debug)
	 * Koru.log(Text.GREEN + "END" + Text.BLUE + " write chunk" + Text.RESET); }
	 * }
	 */

	public Chunk readChunk(int x, int y){
		synchronized(lock){
			if(debug)
				Koru.log(ColorCodes.RED + "BEGIN" + ColorCodes.YELLOW + " read chunk" + ColorCodes.RESET);
			Path path = getPath(x, y);

			long time = TimeUtils.millis();

			try{
				
				Input input = null;
				FileInputStream file = new FileInputStream(path.toFile());
				
				if(compress){
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					
					Lzma.decompress(file, out);
					ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

					input = new Input(in);
					
					out.close();
				}else{
					input = new Input(file);
				}
				Chunk chunk = kryo.readObject(input, Chunk.class);

				input.close();
				file.close();

				if(debug)
					Koru.log("Chunk read time elapsed: " + TimeUtils.timeSinceMillis(time));

				if(debug)
					Koru.log(ColorCodes.GREEN + "END" + ColorCodes.YELLOW + " read chunk" + ColorCodes.RESET);
				return chunk;
			}catch(Exception e){
				Koru.log("Error writing chunk!");
				e.printStackTrace();
			}
			throw new RuntimeException("Error reading chunk!");
		}
	}

	public Chunk generateChunk(int chunkx, int chunky){
		Chunk chunk = Pools.obtain(Chunk.class);
		chunk.set(chunkx, chunky);
		generator.generateChunk(chunk);
		loadedchunks.put(hashCoords(chunkx, chunky), chunk);
		return chunk;
	}

	@Override
	public void unloadChunk(Chunk chunk){
		writeChunk(chunk, 0);
		loadedchunks.remove(hashCoords(chunk.x, chunk.y));
	}

	@Override
	public Chunk getChunk(int chunkx, int chunky){
		Chunk chunk = loadedchunks.get(hashCoords(chunkx, chunky));
		if(chunk == null){
			if(chunkIsSaved(chunkx, chunky)){
				Chunk schunk = readChunk(chunkx, chunky);
				loadedchunks.put(hashCoords(chunkx, chunky), schunk);
				return schunk;
			}else{
				return generateChunk(chunkx, chunky);
			}
		}
		return chunk;
	}

	public Collection<Chunk> getLoadedChunks(){
		return loadedchunks.values();
	}

	public String fileName(int x, int y){
		return filename + hashCoords(x, y) + extension;
	}

	public int totalChunks(){
		return files.size();
	}

	private Path getPath(int x, int y){
		return files.get(fileName(x, y));
	}

	public static long hashCoords(int a, int b){
		return (((long) a) << 32) | (b & 0xffffffffL);
	}
}
