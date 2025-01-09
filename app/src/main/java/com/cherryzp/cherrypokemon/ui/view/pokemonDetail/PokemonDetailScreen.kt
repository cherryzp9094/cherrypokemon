package com.cherryzp.cherrypokemon.ui.view.pokemonDetail

import android.icu.text.DecimalFormat
import android.view.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.cherryzp.cherrypokemon.R
import com.cherryzp.cherrypokemon.enums.PokemonTypeEnum
import com.cherryzp.cherrypokemon.ui.theme.PrimaryFire
import com.cherryzp.domain.model.PokemonDetail
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun PokemonDetailScreen(
    paddingValues: PaddingValues,
    window: Window?,
    pokemonDetail: PokemonDetail,
    pokemonBackgroundColor: Int
) {

    LaunchedEffect(pokemonBackgroundColor) {
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color(pokemonBackgroundColor).toArgb()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        GlideImage(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(pokemonBackgroundColor)),
            imageModel = { pokemonDetail.sprites.other?.officialArtwork?.frontDefault }
        )

        Text(
            modifier = Modifier
                .padding(top = 20.dp)
                .align(CenterHorizontally),
            text = pokemonDetail.name,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .align(CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            pokemonDetail.types.forEach { type ->
                Text(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PokemonTypeEnum.creator(type.type.name).color)
                        .padding(
                            vertical = 6.dp,
                            horizontal = 12.dp
                        ),
                    text = type.type.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth()
                .align(CenterHorizontally)
                .padding(horizontal = 40.dp)
        ) {
            PokemonDetailSizeColumn(
                modifier = Modifier.weight(1f),
                name = stringResource(R.string.height),
                size = "${DecimalFormat("#,###.#")
                    .format(pokemonDetail.height * 0.1f)} m"
            )

            PokemonDetailSizeColumn(
                modifier = Modifier.weight(1f),
                name = stringResource(R.string.weight),
                size = "${DecimalFormat("#,###.#")
                    .format(pokemonDetail.weight * 0.1f)} kg"
            )
        }

        Text(text = pokemonDetail.toString())
    }
}

@Composable
private fun PokemonDetailSizeColumn(
    name: String,
    size: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = size,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = name,
            fontSize = 14.sp
        )
    }
}

@Preview
@Composable
fun PokemonDetailScreenPreview() {
    PokemonDetailScreen(
        paddingValues = PaddingValues(0.dp),
        window = null,
        pokemonDetail = PokemonDetail(
            id = 1,
            name = "bulbasaur",
            baseExperience = 64,
            height = 7f,
            isDefault = true,
            order = 1,
            weight = 69f,
            forms = emptyList(),
            species = PokemonDetail.Species(
                name = "bulbasaur",
                url = "https://pokeapi.co/api/v2/pokemon-species/1/"
            ),
            sprites = PokemonDetail.Sprites(
                backDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                backShiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                frontShiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                other = PokemonDetail.Other(
                    officialArtwork = PokemonDetail.OfficialArtwork(
                        frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        frontShiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"
                    ),
                    showdown = PokemonDetail.Showdown(
                        backDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        backFemale = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        backShiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        backShinyFemale = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        frontDefault = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        frontFemale = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        frontShiny = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
                        frontShinyFemale = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png"
                    )
                )
            ),
            types = emptyList()
        ),
        pokemonBackgroundColor = PrimaryFire.toArgb()
    )
}
