package me.corriekay.pokegoutil.utils.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by gabika on 05/03/17.
 */
public class PokemonHelper {
    // General constants that are set for this file
    private static final File ATTACKER_FILE = new File(System.getProperty("user.dir"), "attackers.txt");
    private static final File DEFENDER_FILE = new File(System.getProperty("user.dir"), "defenders.txt");
    private static final int SAVE_DELAY_SECONDS = 5;

    // Internal needed constants
    //private static final Map<Long, > SAVED_LOCATIONS;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<PokemonMoveset, Integer> ATTACKERS;
    private static final Map<PokemonMoveset, Integer> DEFENDERS;

    // A boolean showing if a save is currently in progress
    private static AtomicBoolean isSaving = new AtomicBoolean(false);

    static {
        ATTACKERS = new ConcurrentHashMap<>();
        DEFENDERS = new ConcurrentHashMap<>();
        load();
    }


    /** Prevent initializing this class. */
    private PokemonHelper() {
    }



    /**
     * Checks if the attacker file exists.
     *
     * @return Weather or not the attacker file exists.
     */
    public static boolean attackerFileExists() {
        return ATTACKER_FILE.exists();
    }

    /**
     * Checks if the defender file exists.
     *
     * @return Weather or not the defender file exists.
     */
    public static boolean defenderFileExists() {
        return DEFENDER_FILE.exists();
    }

    /**
     * Loads saved locations from location.json file.
     */
    private static void load() {

		try (Stream<String> stream = Files.lines(Paths.get(DEFENDER_FILE.getPath()))) {
			DEFENDERS.putAll(stream
					.collect(Collectors.toMap(PokemonMoveset::load, PokemonMoveset::parseValue)));
            System.out.println("Loaded defenders:" + DEFENDERS.size());
		} catch (IOException e) {
			e.printStackTrace();
		}

    }


    public static final class PokemonMoveset {
        public final PokemonId id;
        public final PokemonMove move1;
        public final PokemonMove move2;

        public PokemonMoveset(final PokemonId id, final PokemonMove move1, final PokemonMove move2) {
            this.id = id;
            this.move1 = move1;
            this.move2 = move2;
        }

        private static PokemonMoveset load(final String s) {
            String[] data = s.split("|");
            return new PokemonMoveset(parseId(data[0].trim()), parseMove(data[1].trim() + "_FAST"), parseMove(data[2].trim()));
        }

        private static PokemonId parseId(final String s) {
            return PokemonId.valueOf(s.toUpperCase().replaceAll("\\s","_"));
        }
        private static PokemonMove parseMove(final String s) {
            return PokemonMove.valueOf(s.toUpperCase().replaceAll("\\s","_"));
        }
        private static int parseValue(final String s) {
            return Integer.valueOf(s.split("|")[3]);
        }
    }

}
