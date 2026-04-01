# PokéRekom

An Android application for getting personalized Pokémon team recommendations.

![https://raw.githubusercontent.com/shohiebsense/pokerekom-android/refs/heads/main/Screenshot%202026-04-02%20at%200.17.45.png](https://raw.githubusercontent.com/shohiebsense/pokerekom-android/refs/heads/main/Screenshot%202026-04-02%20at%200.17.45.png)

## Architecture

This project follows **Clean Architecture** with proper separation of concerns:

```
com.shohiebsense.pokerekom/
├── di/                         # Dependency Injection (Hilt modules)
├── domain/                     # Business logic layer
│   ├── model/                  # Domain models
│   └── usecase/                # Use cases
├── data/                       # Data layer
│   ├── local/                  # Local data sources (DataStore, ImageCache)
│   ├── remote/                 # Remote data sources (NetworkClient)
│   └── repository/             # Repository implementations
└── presentation/               # UI layer
    ├── navigation/             # Navigation setup
    ├── recommendation/         # Main recommendation screen
    ├── settings/               # Settings screen
    └── components/             # Reusable UI components
```

## Tech Stack

| Technology | Purpose |
|------------|---------|
| **Jetpack Compose** | Modern declarative UI |
| **Material 3** | Design system |
| **Hilt** | Dependency injection |
| **Navigation Compose** | Screen navigation |
| **StateFlow** | Reactive state management |
| **Coroutines** | Asynchronous programming |
| **OkHttp** | HTTP networking |
| **DataStore** | Preferences storage |
| **Coil** | Image loading |
| **WorkManager** | Background tasks |

---

## Before & After: Code Evolution

This section shows the before (original) vs after (improved) code to demonstrate how modern Android practices were applied.

### Project Structure

#### Before: Flat Structure

All classes in one package - difficult to navigate and maintain:

```
com.shohiebsense.pokerekom/
├── MainActivity.kt              # UI + logic mixed
├── RecommendViewModels.kt       # ViewModel + manual DI
├── RecommendRepository.kt        # Repository + business logic
├── NetworkClient.kt            # Network layer
├── ImageCache.kt               # Cache logic
├── PrefsDatastore.kt           # DataStore wrapper
├── Models.kt                   # Data classes
├── RecommendUploadWorker.kt     # Background worker
└── ui/
    └── theme/                   # Theme files only
        ├── Theme.kt
        ├── Color.kt
        └── Type.kt
```

#### After: Clean Architecture Layers

```
com.shohiebsense.pokerekom/
├── di/                          # Hilt modules
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── domain/                      # Business logic (no Android deps)
│   ├── model/
│   │   ├── Result.kt           # Error handling wrapper
│   │   ├── Pokemon.kt
│   │   └── TeamResponse.kt
│   └── usecase/
│       ├── GetRecommendationUseCase.kt
│       ├── GetCachedTeamUseCase.kt
│       └── CacheImagesUseCase.kt
├── data/                        # Data layer
│   ├── local/
│   │   ├── ImageCache.kt
│   │   └── PrefsDataStore.kt
│   ├── remote/
│   │   └── NetworkClient.kt
│   └── repository/
│       ├── PokemonRepository.kt  # Interface
│       └── PokemonRepositoryImpl.kt
├── presentation/                # UI layer
│   ├── navigation/
│   │   ├── Screen.kt
│   │   └── PokeNavHost.kt
│   ├── recommendation/
│   │   ├── RecommendationScreen.kt
│   │   ├── RecommendationViewModel.kt
│   │   └── RecommendationUiState.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   └── components/
│       └── PokemonCard.kt
├── PokerekomApp.kt             # Hilt Application
├── MainActivity.kt              # Clean Activity
└── ui/theme/                    # Theme files
```

---

### Dependency Injection

#### Before: Manual Instantiation

```kotlin
// OLD: RecommendViewModels.kt
class RecommendViewModel(application: Application) : AndroidViewModel(application) {
    // Manual instantiation - creates new instances each time
    private val repo = RecommendRepository(application)
    private val cache = ImageCache(getApplication())
    
    fun requestRecommendation(payloadJson: String? = "{}") {
        viewModelScope.launch {
            // Logic mixed in ViewModel
            _uiState.value = UiState.Loading
            try {
                val team = repo.fetchTeam(payloadJson)  // Direct call
                _uiState.value = UiState.Success(team)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

**Problems:**
- `RecommendRepository` created every time ViewModel is created
- Cannot mock for testing
- Tight coupling between classes
- Hard to manage dependencies across the app

#### After: Hilt Injection

```kotlin
// NEW: RecommendationViewModel.kt
@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val getRecommendationUseCase: GetRecommendationUseCase,
    private val getCachedTeamUseCase: GetCachedTeamUseCase,
    private val cacheImagesUseCase: CacheImagesUseCase,
    private val imageCache: ImageCache
) : ViewModel() {
    
    fun requestRecommendation(payloadJson: String? = "{}") {
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            
            // Use case handles business logic
            val result = getRecommendationUseCase(payloadJson)
            
            result.onSuccess { team ->
                _uiState.value = RecommendationUiState.Success(team, loadCachedUris(team))
            }.onError { error ->
                _uiState.value = RecommendationUiState.Error(error.message ?: "Unknown error")
            }
        }
    }
}
```

**Benefits:**
- Dependencies provided by Hilt container
- Single instance of repository (singleton)
- Easy to mock for unit testing
- Clear dependency graph
- Lifecycle-aware (ViewModel-scoped deps)

---

### Hilt Setup

#### Application Class

```kotlin
// Before: No Application class
// MainActivity just extended ComponentActivity

// After: PokerekomApp.kt
@HiltAndroidApp
class PokerekomApp : Application()
```

#### Hilt Modules

```kotlin
// di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideImageCache(@ApplicationContext context: Context): ImageCache {
        return ImageCache(context)
    }

    @Provides
    @Singleton
    fun providePrefsDataStore(@ApplicationContext context: Context): PrefsDataStore {
        return PrefsDataStore(context)
    }
}
```

```kotlin
// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkClient(okHttpClient: OkHttpClient): NetworkClient {
        return NetworkClient(okHttpClient)
    }
}
```

```kotlin
// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        impl: PokemonRepositoryImpl
    ): PokemonRepository
}
```

---

## UI States Explained

### What Are UI States?

UI State represents everything the UI needs to render itself at any given moment. It's the single source of truth for the UI layer.

### Why Use Sealed Interfaces for UI State?

```kotlin
sealed interface RecommendationUiState {
    data object Idle : RecommendationUiState
    data object Loading : RecommendationUiState
    data class Success(val team: List<String>, val imageUris: Map<String, Uri>) : RecommendationUiState
    data class Error(val message: String) : RecommendationUiState
}
```

Benefits:
- **Type Safety** - Compiler knows all possible states
- **Exhaustive When** - Can't forget to handle a state
- **Immutable** - Each state is immutable, preventing bugs
- **Self-Documenting** - Clear what states exist

### The Four States Explained

#### 1. Idle State
```kotlin
data object Idle : RecommendationUiState
```
- Initial state when app launches
- No action has been taken yet
- UI shows "Tap the button to get recommendations" message

#### 2. Loading State
```kotlin
data object Loading : RecommendationUiState
```
- Request is in progress
- Button shows CircularProgressIndicator
- User cannot trigger another request (button disabled)

#### 3. Success State
```kotlin
data class Success(
    val team: List<String>,              // List of Pokémon names
    val imageUris: Map<String, Uri>     // Cached image file URIs
) : RecommendationUiState
```
- Data received successfully
- LazyVerticalGrid displays Pokémon cards
- Each card shows image (from cache or remote) and name

#### 4. Error State
```kotlin
data class Error(val message: String) : RecommendationUiState
```
- Something went wrong (network error, parse error, etc.)
- UI shows error message in red
- User can try again by tapping the button

### UI State Flow in ViewModel

```kotlin
class RecommendationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Idle)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    fun requestRecommendation(payloadJson: String? = "{}") {
        if (_uiState.value is RecommendationUiState.Loading) return  // Prevent double requests
        
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading  // Show loading
            
            val result = getRecommendationUseCase(payloadJson)
            
            result.onSuccess { team ->
                _uiState.value = RecommendationUiState.Success(
                    team = team,
                    imageUris = loadCachedUris(team)
                )
            }.onError { error ->
                _uiState.value = RecommendationUiState.Error(
                    message = error.message ?: "Unknown error"
                )
            }
        }
    }
}
```

### UI State Flow Diagram

```
User taps button
       │
       ▼
┌─────────────────┐
│   Loading?      │──── No ────▶ Ignore (already loading)
└────────┬────────┘
         │ Yes
         ▼
┌─────────────────┐
│  _uiState.value │
│   = Loading     │
└────────┬────────┘
         │
         ▼
   Compose observes
   via collectAsState()
         │
         ▼
┌─────────────────┐
│ Button shows    │
│ CircularProgress │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ API call via    │
│ UseCase         │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
 Success   Error
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│ Success│ │ Error  │
│ state   │ │ state  │
└────────┘ └────────┘
    │         │
    ▼         ▼
 Grid shows  Error text
 Pokemon     displayed
```

### Observing State in Compose

```kotlin
@Composable
fun RecommendationScreen(
    viewModel: RecommendationViewModel = hiltViewModel()
) {
    // Collect state as Compose State
    val uiState by viewModel.uiState.collectAsState()

    // Handle each state
    when (val state = uiState) {
        is RecommendationUiState.Idle -> {
            // Show initial message
        }
        is RecommendationUiState.Loading -> {
            // Show loading indicator
        }
        is RecommendationUiState.Success -> {
            // Show team grid
            state.team.forEach { pokemon ->
                PokemonCard(name = pokemon)
            }
        }
        is RecommendationUiState.Error -> {
            // Show error message
            Text("Error: ${state.message}")
        }
    }
}
```

---

## Navigation

### Before: Single Screen

```kotlin
// OLD: MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: RecommendViewModel = viewModel()
            MainScreen(vm)  // Only one screen
        }
    }
}
```

### After: Multi-Screen Navigation

```kotlin
// presentation/navigation/Screen.kt
sealed class Screen(val route: String) {
    data object Recommendation : Screen("recommendation")
    data object Settings : Screen("settings")
}
```

```kotlin
// presentation/navigation/PokeNavHost.kt
@Composable
fun PokeNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Recommendation.route
    ) {
        composable(route = Screen.Recommendation.route) {
            RecommendationScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
```

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokerekomTheme {
                val navController = rememberNavController()
                PokeNavHost(navController = navController)
            }
        }
    }
}
```

---

## Use Cases

### Before: Logic in Repository

```kotlin
// OLD: Logic mixed in repository
class RecommendRepository(private val ctx: Context) {
    suspend fun fetchTeam(payloadJson: String?): List<String> = withContext(Dispatchers.IO) {
        // Business logic mixed with data access
        val req = NetworkClient.buildPostRequest(url, payloadJson)
        val resp = NetworkClient.execute(req)
        // ... parsing logic here
    }
}
```

### After: Use Cases for Business Logic

```kotlin
// domain/usecase/GetRecommendationUseCase.kt
class GetRecommendationUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(payloadJson: String? = "{}"): Result<List<String>> {
        return try {
            val team = repository.fetchTeam(payloadJson)
            Result.Success(team)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

```kotlin
// domain/usecase/GetCachedTeamUseCase.kt
class GetCachedTeamUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(): Result<List<String>?> {
        return try {
            val team = repository.loadCachedTeam()
            Result.Success(team)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

```kotlin
// domain/usecase/CacheImagesUseCase.kt
class CacheImagesUseCase @Inject constructor(
    private val repository: PokemonRepository
) {
    suspend operator fun invoke(team: List<String>, maxRetries: Int = 3): Result<Unit> {
        return try {
            repository.ensureImagesCached(team, maxRetries)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
```

---

## Error Handling

### Before: Basic Try-Catch

```kotlin
// OLD
try {
    val team = repo.fetchTeam(payloadJson)
    _uiState.value = UiState.Success(team)
} catch (e: Exception) {
    _uiState.value = UiState.Error(e.message ?: "Unknown error")
}
```

### After: Result Wrapper

```kotlin
// domain/model/Result.kt
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
    
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
}
```

---

## Building

```bash
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## Best Practices Applied

| Practice | Before | After |
|----------|--------|-------|
| **Architecture** | Flat structure | Clean Architecture layers |
| **DI** | Manual instantiation | Hilt injection |
| **Navigation** | Single screen | Jetpack Navigation |
| **Use Cases** | Logic in repository | Separate use cases |
| **Error Handling** | Basic try-catch | Result wrapper |
| **UI State** | Mutable variables | Sealed interfaces + StateFlow |
| **Package Organization** | All in root | Feature-based packages |
| **Testing** | Hard to test | Easy to mock dependencies |
