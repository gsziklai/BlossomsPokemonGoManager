package me.corriekay.pokegoutil.utils.pokemon;

import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.main.PokemonMeta;

import POGOProtos.Enums.PokemonMoveOuterClass;
import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;
import POGOProtos.Settings.Master.MoveSettingsOuterClass.MoveSettings;

/**
 * Created by gabor_sziklai on 2017.03.02..
 */
public class PokemonCalculations {


    /**
     * Calculates the ms for current move.
     *
     * @param p       A Pokemon object.
     * @param primary If it should be calculated for the primary more or the secondary.
     * @return The clean dps.
     */
    public static int msForMove(final Pokemon p, final boolean primary) {
        final PokemonMoveOuterClass.PokemonMove move = primary ? p.getMove1() : p.getMove2();
        return msForMove(move);
    }

    /**
     * Calculates the ms for current move.
     *
     * @param move      The move to calculate the dps for.
     * @return The clean dps.
     */
    private static int msForMove(final PokemonMove move) {
        final MoveSettings moveMeta = PokemonMeta.getMoveSettings(move);
        return moveMeta.getDurationMs();
    }


}
