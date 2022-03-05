package com.ky.auto_pkg

import com.aliyun.oss.ClientBuilderConfiguration
import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.google.gson.GsonBuilder
import com.ky.auto_pkg.http.AddHeaderInterceptor
import com.ky.auto_pkg.http.BaseResponse
import com.ky.auto_pkg.http.BeanTypeDeserializer
import com.ky.auto_pkg.http.IAppService
import com.ky.auto_pkg.model.BuildConfig
import com.ky.auto_pkg.model.StsToken
import com.ky.auto_pkg.utils.EnvUtils
import com.ky.auto_pkg.utils.FileUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by 王金瑞
 * 2021/11/26
 * 19:58
 * com.ky.auto_pkg
 */
object Core {
    private const val CORE_POOL_SIZE = 3
    private const val MAXI_MUM_POOL_SIZE = 20
    val GSON = GsonBuilder().registerTypeAdapter(BaseResponse::class.java, BeanTypeDeserializer())
        .serializeNulls().create()!!
    val APP_SERVICE: IAppService by lazy {
        val builder = OkHttpClient.Builder()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addNetworkInterceptor(interceptor)
        // 加入拦截器
        builder.addInterceptor(AddHeaderInterceptor()).connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        Retrofit.Builder().baseUrl(ConfigConstants.API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
            .client(builder.build())
            .build().create(IAppService::class.java)
    }
    val THREAD_POOL = ThreadPoolExecutor(
        CORE_POOL_SIZE, MAXI_MUM_POOL_SIZE, 20L, TimeUnit.SECONDS, SynchronousQueue()
    )

    fun createOssClient(stsToken: StsToken): OSS {
        val configuration = ClientBuilderConfiguration()
        configuration.maxConnections = 200
        // 超时120秒
        configuration.socketTimeout = 120000
        // 失败重试3次
        configuration.maxErrorRetry = 3
        // 设置是否支持将自定义域名作为Endpoint，默认支持。
        configuration.isSupportCname = true

        return OSSClientBuilder().build(
            stsToken.endpoint,
            stsToken.access_key,
            stsToken.access_Secret,
            stsToken.security_token,
            configuration
        )
    }

    /**
     * 根据命令构建指定
     */
    @Throws(Exception::class)
    fun buildProcess(command: String): Process {
        val process = Runtime.getRuntime().exec(command)

        val buildLogBr =
            BufferedReader(InputStreamReader(process.inputStream, StandardCharsets.UTF_8))
        val errLogBr =
            BufferedReader(InputStreamReader(process.errorStream, StandardCharsets.UTF_8))
        FileUtils.writeAsyncContent(buildLogBr)
        FileUtils.writeAsyncContent(errLogBr)

        return process
    }

    @Throws(Exception::class)
    fun initConfigProperties(buildConfig: BuildConfig) {
        val properties = Properties()
        properties.load(
            javaClass.getResourceAsStream(
                "/" + ConfigConstants.CONFIG_URL_CONFIG_FILE_NAME
            )
        )

        // build static params
        ConfigConstants.ENV_ASSEMBLE_NAME =
            EnvUtils.getAssembleName(properties.getProperty(buildConfig.assembleType))
        ConfigConstants.ENV_SERVER_SERVER_NAME =
            EnvUtils.getServerName(properties.getProperty(buildConfig.serverEnvType))
        ConfigConstants.URL_NGINX_PREFIX =
            properties.getProperty(ConfigConstants.CONFIG_URL_NGINX_KEY)
        ConfigConstants.API_BASE_URL = properties.getProperty(
            ConfigConstants.CONFIG_API_KEY_PREFIX
                    + buildConfig.serverEnvType.uppercase()
        )
        ConfigConstants.TOKEN = properties.getProperty(
            (ConfigConstants.CONFIG_TOKEN_KEY_PREFIX
                    + buildConfig.serverEnvType.uppercase())
        )

        // 是否为测试打包模式
        val botKey = if (buildConfig.isPkgTest) ConfigConstants.CONFIG_FEISHU_URL_KEY + "_TEST"
        else ConfigConstants.CONFIG_FEISHU_URL_KEY

        ConfigConstants.FEISHU_BOT_WEB_HOOK_URL =
            properties.getProperty(botKey)
    }
}