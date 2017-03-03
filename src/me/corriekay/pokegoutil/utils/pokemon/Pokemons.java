package me.corriekay.pokegoutil.utils.pokemon;

import java.util.ArrayList;
import java.util.List;

import com.pokegoapi.api.pokemon.Evolutions;

import POGOProtos.Enums.PokemonIdOuterClass;
import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;

/**
 * Created by gabor_sziklai on 2017.03.03..
 */
public class Pokemons {
    public static PokemonMove getEvolvedPerfectMove1ForOffense(final PokemonId pokemonId) {
        return PokemonPerformanceCache.getStats(getHighestEvolved(pokemonId)).gymOffense.move1;
    }
    public static PokemonMove getEvolvedPerfectMove1ForDefense(final PokemonId pokemonId) {
        return PokemonPerformanceCache.getStats(getHighestEvolved(pokemonId)).gymDefense.move1;
    }
    public static PokemonMove getEvolvedPerfectMove2ForOffense(final PokemonId pokemonId) {
        return PokemonPerformanceCache.getStats(getHighestEvolved(pokemonId)).gymOffense.move2;
    }
    public static PokemonMove getEvolvedPerfectMove2ForDefense(final PokemonId pokemonId) {
        return PokemonPerformanceCache.getStats(getHighestEvolved(pokemonId)).gymDefense.move2;
    }

    public static PokemonId getHighestEvolved(final PokemonId pokemonId) {
        final List<PokemonIdOuterClass.PokemonId> highest = Evolutions.getHighest(pokemonId);
        return highest.get(0);
    }


    public static PokemonId getNextEvolution(PokemonId pokemonId) {
        switch (pokemonId) {
            case EEVEE:
                return PokemonId.JOLTEON;
            default:
                List<PokemonId> evols = Evolutions.getEvolutions(pokemonId);
                return evols.isEmpty() ? null : evols.get(0);
        }
    }

    public static List<PokemonId> getEvolutions(PokemonId pokemonId) {
        List<PokemonId> evols = Evolutions.getEvolutions(pokemonId);
        List<PokemonId> ret = new ArrayList<>(evols);
        for ( PokemonId evol : evols ) {
            ret.addAll(Evolutions.getEvolutions(evol));
        }
        return ret;
    }


}
