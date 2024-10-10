package com.example.runpal.server

import com.example.runpal.ServerException
import com.example.runpal.SERVER_ADDRESS
import com.example.runpal.repositories.LoginManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UnauthorizedClient
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthorizedClient
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UnauthorizedRetrofit
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthorizedRetrofit

@Module
@InstallIn(SingletonComponent::class)
class ServerModule {

    @Provides
    fun provideX509TrustManager(): X509TrustManager { return MyTrustManager()}

    @Provides
    fun provideSSLSocketFactory(manager: X509TrustManager): SSLSocketFactory {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(manager), null)
        return sslContext.socketFactory
    }
    @Provides
    @UnauthorizedClient
    fun provideUnauthorizedClient(sslSocketFactory: SSLSocketFactory,
                                  trustManager: X509TrustManager): OkHttpClient {
        val httpInterceptor = HttpLoggingInterceptor()
        httpInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            //.addInterceptor(httpInterceptor)
            .build()
        return client
    }
    @Provides
    @AuthorizedClient
    fun provideAuthorizedClient(@UnauthorizedClient client: OkHttpClient,
                                loginManager: LoginManager): OkHttpClient {
        val authorizationInterceptor = object: Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val token = loginManager.currentToken() ?: throw ServerException("Token expired.")
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                return chain.proceed(request)
            }
        }
        return client.newBuilder()
            .addInterceptor(authorizationInterceptor)
            .build()
    }

    @Singleton
    @Provides
    @UnauthorizedRetrofit
    fun provideUnauthorizedRetrofit(
        @UnauthorizedClient client: OkHttpClient
    ): Retrofit {
        val scalars = ScalarsConverterFactory.create()
        val gson = GsonBuilder()
            .create()
        return Retrofit.Builder()
            .baseUrl(SERVER_ADDRESS)
            .client(client)
            .addConverterFactory(scalars)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    @Singleton
    @Provides
    @AuthorizedRetrofit
    fun provideAuthorizedRetrofit(
        @UnauthorizedRetrofit retrofit: Retrofit,
        @AuthorizedClient client: OkHttpClient
    ): Retrofit {
        return retrofit.newBuilder().client(client).build()
    }

    @Singleton
    @Provides
    fun provideLoginApi(@UnauthorizedRetrofit retrofit: Retrofit): LoginApi = retrofit.create(LoginApi::class.java)
    @Singleton
    @Provides
    fun provideRunApi(@AuthorizedRetrofit retrofit: Retrofit): RunApi = retrofit.create(RunApi::class.java)
    @Singleton
    @Provides
    fun provideUserApi(@AuthorizedRetrofit retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)
    @Singleton
    @Provides
    fun provideUploadApi(@AuthorizedRetrofit retrofit: Retrofit): UploadApi = retrofit.create(UploadApi::class.java)
    @Singleton
    @Provides
    fun provideRoomApi(@AuthorizedRetrofit retrofit: Retrofit): RoomApi = retrofit.create((RoomApi::class.java))
    @Singleton
    @Provides
    fun provideEventApi(@AuthorizedRetrofit retrofit: Retrofit): EventApi = retrofit.create((EventApi::class.java))
}


class MyTrustManager: X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}