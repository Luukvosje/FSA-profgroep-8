import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.profgroep8.rmc_app.R

@Composable
fun LogoComponent() {
    Box (
        modifier = Modifier.fillMaxWidth().padding(0.dp, 40.dp, 0.dp, 0.dp),
        contentAlignment = Alignment.Center,
    ){
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "logo")
    }
}