package coty.band.app.di

import android.content.Context
import androidx.room.Room
import coty.band.app.data.AuthRepositoryImpl
import coty.band.app.data.MeasurementRepositoryImpl
import coty.band.app.data.local.AppDatabase
import coty.band.app.data.local.MeasurementDao
import coty.band.app.data.remote.AnalyzeApi
import coty.band.app.data.remote.AuthApi
import coty.band.app.data.remote.AuthInterceptor
import coty.band.app.data.remote.MeasurementApi
import coty.band.app.domain.AuthRepository
import coty.band.app.domain.MeasurementRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://beggarly-emerging-sharksucker.cloudpub.ru/api/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideAnalyzeApi(retrofit: Retrofit): AnalyzeApi =
        retrofit.create(AnalyzeApi::class.java)

    @Provides @Singleton
    fun provideMeasurementApi(retrofit: Retrofit): MeasurementApi =
        retrofit.create(MeasurementApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "coty_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideMeasurementDao(db: AppDatabase): MeasurementDao = db.measurementDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindMeasurementRepository(impl: MeasurementRepositoryImpl): MeasurementRepository
}
