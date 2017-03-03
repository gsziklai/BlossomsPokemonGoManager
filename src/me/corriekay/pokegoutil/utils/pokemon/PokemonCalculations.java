package me.corriekay.pokegoutil.utils.pokemon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pokegoapi.api.pokemon.Pokemon;
import com.pokegoapi.main.PokemonMeta;

import me.corriekay.pokegoutil.utils.Utilities;

import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import POGOProtos.Enums.PokemonMoveOuterClass;
import POGOProtos.Enums.PokemonMoveOuterClass.PokemonMove;
import POGOProtos.Settings.Master.MoveSettingsOuterClass.MoveSettings;
import POGOProtos.Settings.Master.Pokemon.StatsAttributesOuterClass.StatsAttributes;
import POGOProtos.Settings.Master.PokemonSettingsOuterClass.PokemonSettings;

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

    public static double weaveRating(final Pokemon p, final boolean defense) {
        final PokemonSettings pMeta = PokemonMeta.getPokemonSettings(p.getPokemonId());
        return weaveRating(p.getPokemonId(), p.getMove1(), p.getMove2(), defense);
    }

    public static double weaveRating(final PokemonId pokemonId, final PokemonMove move1, final PokemonMove move2, boolean defense) {
        final PokemonSettings pMeta = PokemonMeta.getPokemonSettings(pokemonId);
        double highestDps = 0;
        final List<PokemonMove> moves1 = pMeta.getQuickMovesList();
        final List<PokemonMove> moves2 = pMeta.getCinematicMovesList();
        for (final PokemonMove m1 : moves1) {
            for (final PokemonMove m2 : moves2) {
                double dps = defense ? PokemonCalculationUtils.weaveDps(pokemonId, m1, m2, PokemonCalculationUtils.MOVE_2_ADDITIONAL_DELAY) : Math.max(
                        PokemonCalculationUtils.dpsForMove(pokemonId, m1, true) * PokemonCalculationUtils.WEAVE_LENGTH_SECONDS,
                        PokemonCalculationUtils.weaveDps(pokemonId, m1, m2, 0)
                );
                if (dps > highestDps){
                    highestDps = dps;
                }
            }
        }
        // Now rate it
        final double currentDps = defense ? PokemonCalculationUtils.weaveDps(pokemonId, move1, move2, PokemonCalculationUtils.MOVE_2_ADDITIONAL_DELAY) :
                Math.max(
                PokemonCalculationUtils.dpsForMove(pokemonId, move1, true) * PokemonCalculationUtils.WEAVE_LENGTH_SECONDS,
                PokemonCalculationUtils.weaveDps(pokemonId, move1, move2, 0)
        );
        return Utilities.percentage(currentDps, highestDps);
    }

    public static double gymPrestige(final Pokemon p) {//, final PokemonMove move1, final PokemonMove move2, final int attackIV, final int defenseIV, final int staminaIV) {
        return gymPrestige(p.getPokemonId(), p.getMove1(), p.getMove2(), p.getIndividualAttack(), p.getIndividualDefense(), p.getIndividualStamina());
    }


    public static double gymPrestige(final PokemonId pokemonId, final PokemonMove move1, final PokemonMove move2, final int attackIV, final int defenseIV, final int staminaIV) {
        final StatsAttributes pMeta = PokemonMeta.getPokemonSettings(pokemonId).getStats();
        return Math.max(PokemonCalculationUtils.dpsForMove(pokemonId, move1, true) * PokemonCalculationUtils.WEAVE_LENGTH_SECONDS,
                        PokemonCalculationUtils.weaveDps(pokemonId, move1, move2, 0))
                * Math.pow(pMeta.getBaseStamina() + staminaIV, 0.25)
                * Math.pow(pMeta.getBaseDefense() + defenseIV, 0.25)
                / Math.pow(pMeta.getBaseAttack() + attackIV, 0.5);
    }



    public static double prestigeRating(final Pokemon p) {
        return prestigeRating(p.getPokemonId(), p.getIndividualAttack(), p.getIndividualDefense(), p.getIndividualStamina());
    }

    public static double prestigeRating(final PokemonId pokemonId, final int attackIV, final int defenseIV, final int staminaIV) {
        //max: ((baseSta+15) * (baseDef+15))^.25 / (baseAtt)^.5
        //min: (baseSta * baseDef)^.25 / (baseAtt+15)^.5
        //act: ((baseSta+ivSta) * (baseDef+ivDef))^.25 / (baseAtt+ivAtt)^.5
        final StatsAttributes pMeta = PokemonMeta.getPokemonSettings(pokemonId).getStats();
        final double prMax = Math.pow(pMeta.getBaseStamina() + PokemonUtils.MAX_IV, 0.25)
                * Math.pow(pMeta.getBaseDefense() + PokemonUtils.MAX_IV, 0.25)
                / Math.pow(pMeta.getBaseAttack(), 0.5);
        final double prMin = Math.pow(pMeta.getBaseStamina(), 0.25)
                * Math.pow(pMeta.getBaseDefense(), 0.25)
                / Math.pow(pMeta.getBaseAttack()+ PokemonUtils.MAX_IV, 0.5);
        final double prIv = Math.pow(pMeta.getBaseStamina() + staminaIV, 0.25)
                * Math.pow(pMeta.getBaseDefense() + defenseIV, 0.25)
                / Math.pow(pMeta.getBaseAttack() + attackIV, 0.5);
        return (prIv - prMin) / (prMax - prMin);

    }
    public static int getHp(final PokemonId pokemonId, final float level, final int staminaIV) {
        final StatsAttributes pMeta = PokemonMeta.getPokemonSettings(pokemonId).getStats();
        float multiplier = LEVEL_CP_MULTIPLIER.get(level);
        int stamina = pMeta.getBaseStamina() + staminaIV;
        return (int) (stamina * multiplier);
    }

    private static final Map<Float, Float> LEVEL_CP_MULTIPLIER = new HashMap<>();

    static {
        LEVEL_CP_MULTIPLIER.put(1f, 0.094f);
        LEVEL_CP_MULTIPLIER.put(1.5f, 0.135137432f);
        LEVEL_CP_MULTIPLIER.put(2f, 0.16639787f);
        LEVEL_CP_MULTIPLIER.put(2.5f, 0.192650919f);
        LEVEL_CP_MULTIPLIER.put(3f, 0.21573247f);
        LEVEL_CP_MULTIPLIER.put(3.5f, 0.236572661f);
        LEVEL_CP_MULTIPLIER.put(4f, 0.25572005f);
        LEVEL_CP_MULTIPLIER.put(4.5f, 0.273530381f);
        LEVEL_CP_MULTIPLIER.put(5f, 0.29024988f);
        LEVEL_CP_MULTIPLIER.put(5.5f, 0.306057377f);
        LEVEL_CP_MULTIPLIER.put(6f, 0.3210876f);
        LEVEL_CP_MULTIPLIER.put(6.5f, 0.335445036f);
        LEVEL_CP_MULTIPLIER.put(7f, 0.34921268f);
        LEVEL_CP_MULTIPLIER.put(7.5f, 0.362457751f);
        LEVEL_CP_MULTIPLIER.put(8f, 0.37523559f);
        LEVEL_CP_MULTIPLIER.put(8.5f, 0.387592406f);
        LEVEL_CP_MULTIPLIER.put(9f, 0.39956728f);
        LEVEL_CP_MULTIPLIER.put(9.5f, 0.411193551f);
        LEVEL_CP_MULTIPLIER.put(10f, 0.42250001f);
        LEVEL_CP_MULTIPLIER.put(10.5f, 0.432926419f);
        LEVEL_CP_MULTIPLIER.put(11f, 0.44310755f);
        LEVEL_CP_MULTIPLIER.put(11.5f, 0.453059958f);
        LEVEL_CP_MULTIPLIER.put(12f, 0.46279839f);
        LEVEL_CP_MULTIPLIER.put(12.5f, 0.472336083f);
        LEVEL_CP_MULTIPLIER.put(13f, 0.48168495f);
        LEVEL_CP_MULTIPLIER.put(13.5f, 0.4908558f);
        LEVEL_CP_MULTIPLIER.put(14f, 0.49985844f);
        LEVEL_CP_MULTIPLIER.put(14.5f, 0.508701765f);
        LEVEL_CP_MULTIPLIER.put(15f, 0.51739395f);
        LEVEL_CP_MULTIPLIER.put(15.5f, 0.525942511f);
        LEVEL_CP_MULTIPLIER.put(16f, 0.53435433f);
        LEVEL_CP_MULTIPLIER.put(16.5f, 0.542635767f);
        LEVEL_CP_MULTIPLIER.put(17f, 0.55079269f);
        LEVEL_CP_MULTIPLIER.put(17.5f, 0.558830576f);
        LEVEL_CP_MULTIPLIER.put(18f, 0.56675452f);
        LEVEL_CP_MULTIPLIER.put(18.5f, 0.574569153f);
        LEVEL_CP_MULTIPLIER.put(19f, 0.58227891f);
        LEVEL_CP_MULTIPLIER.put(19.5f, 0.589887917f);
        LEVEL_CP_MULTIPLIER.put(20f, 0.59740001f);
        LEVEL_CP_MULTIPLIER.put(20.5f, 0.604818814f);
        LEVEL_CP_MULTIPLIER.put(21f, 0.61215729f);
        LEVEL_CP_MULTIPLIER.put(21.5f, 0.619399365f);
        LEVEL_CP_MULTIPLIER.put(22f, 0.62656713f);
        LEVEL_CP_MULTIPLIER.put(22.5f, 0.633644533f);
        LEVEL_CP_MULTIPLIER.put(23f, 0.64065295f);
        LEVEL_CP_MULTIPLIER.put(23.5f, 0.647576426f);
        LEVEL_CP_MULTIPLIER.put(24f, 0.65443563f);
        LEVEL_CP_MULTIPLIER.put(24.5f, 0.661214806f);
        LEVEL_CP_MULTIPLIER.put(25f, 0.667934f);
        LEVEL_CP_MULTIPLIER.put(25.5f, 0.674577537f);
        LEVEL_CP_MULTIPLIER.put(26f, 0.68116492f);
        LEVEL_CP_MULTIPLIER.put(26.5f, 0.687680648f);
        LEVEL_CP_MULTIPLIER.put(27f, 0.69414365f);
        LEVEL_CP_MULTIPLIER.put(27.5f, 0.700538673f);
        LEVEL_CP_MULTIPLIER.put(28f, 0.70688421f);
        LEVEL_CP_MULTIPLIER.put(28.5f, 0.713164996f);
        LEVEL_CP_MULTIPLIER.put(29f, 0.71939909f);
        LEVEL_CP_MULTIPLIER.put(29.5f, 0.725571552f);
        LEVEL_CP_MULTIPLIER.put(30f, 0.7317f);
        LEVEL_CP_MULTIPLIER.put(30.5f, 0.734741009f);
        LEVEL_CP_MULTIPLIER.put(31f, 0.73776948f);
        LEVEL_CP_MULTIPLIER.put(31.5f, 0.740785574f);
        LEVEL_CP_MULTIPLIER.put(32f, 0.74378943f);
        LEVEL_CP_MULTIPLIER.put(32.5f, 0.746781211f);
        LEVEL_CP_MULTIPLIER.put(33f, 0.74976104f);
        LEVEL_CP_MULTIPLIER.put(33.5f, 0.752729087f);
        LEVEL_CP_MULTIPLIER.put(34f, 0.75568551f);
        LEVEL_CP_MULTIPLIER.put(34.5f, 0.758630378f);
        LEVEL_CP_MULTIPLIER.put(35f, 0.76156384f);
        LEVEL_CP_MULTIPLIER.put(35.5f, 0.764486065f);
        LEVEL_CP_MULTIPLIER.put(36f, 0.76739717f);
        LEVEL_CP_MULTIPLIER.put(36.5f, 0.770297266f);
        LEVEL_CP_MULTIPLIER.put(37f, 0.7731865f);
        LEVEL_CP_MULTIPLIER.put(37.5f, 0.776064962f);
        LEVEL_CP_MULTIPLIER.put(38f, 0.77893275f);
        LEVEL_CP_MULTIPLIER.put(38.5f, 0.781790055f);
        LEVEL_CP_MULTIPLIER.put(39f, 0.78463697f);
        LEVEL_CP_MULTIPLIER.put(39.5f, 0.787473578f);
        LEVEL_CP_MULTIPLIER.put(40f, 0.79030001f);
    }

}
