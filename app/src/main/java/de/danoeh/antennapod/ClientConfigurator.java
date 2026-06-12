package de.danoeh.antennapod;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import de.danoeh.antennapod.net.download.service.episode.autodownload.AutoDownloadManagerImpl;
import de.danoeh.antennapod.net.download.service.feed.FeedUpdateManagerImpl;
import de.danoeh.antennapod.net.download.serviceinterface.AutoDownloadManager;
import de.danoeh.antennapod.net.download.serviceinterface.FeedUpdateManager;
import de.danoeh.antennapod.net.sync.service.SynchronizationQueueImpl;
import de.danoeh.antennapod.net.sync.serviceinterface.SynchronizationQueue;
import de.danoeh.antennapod.storage.preferences.SynchronizationSettings;
import de.danoeh.antennapod.storage.preferences.SynchronizationCredentials;
import de.danoeh.antennapod.storage.preferences.PlaybackPreferences;
import de.danoeh.antennapod.storage.preferences.SleepTimerPreferences;
import de.danoeh.antennapod.storage.preferences.UsageStatistics;
import de.danoeh.antennapod.net.common.BasicAuthorizationInterceptor;
import de.danoeh.antennapod.net.common.UserAgentInterceptor;
import de.danoeh.antennapod.storage.preferences.UserPreferences;
import de.danoeh.antennapod.net.common.AntennapodHttpClient;
import de.danoeh.antennapod.net.download.serviceinterface.DownloadServiceInterface;
import de.danoeh.antennapod.net.download.service.feed.DownloadServiceInterfaceImpl;
import de.danoeh.antennapod.net.common.NetworkUtils;
import de.danoeh.antennapod.net.ssl.SslProviderInstaller;
import de.danoeh.antennapod.storage.database.DBReader;
import de.danoeh.antennapod.storage.database.PodDBAdapter;

import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.ui.notifications.NotificationUtils;
import java.io.File;
import java.net.URL;

public class ClientConfigurator {
    private static final String TAG = "ClientConfigurator";
    private static boolean initialized = false;

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            UserAgentInterceptor.USER_AGENT = "AntennaPod/" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        PodDBAdapter.init(context);
        UserPreferences.init(context);
        SynchronizationCredentials.init(context);
        SynchronizationSettings.init(context);
        UsageStatistics.init(context);
        PlaybackPreferences.init(context);
        SslProviderInstaller.install(context);
        NetworkUtils.init(context);
        DownloadServiceInterface.setImpl(new DownloadServiceInterfaceImpl());
        FeedUpdateManager.setInstance(new FeedUpdateManagerImpl());
        AutoDownloadManager.setInstance(new AutoDownloadManagerImpl());
        SynchronizationQueue.setInstance(new SynchronizationQueueImpl(context));
        BasicAuthorizationInterceptor.setCredentialsProvider(url -> {
            try {
                String host = new URL(url).getHost();
                for (Feed feed : DBReader.getFeedList()) {
                    if (feed.getPreferences() != null
                            && feed.getDownloadUrl() != null
                            && new URL(feed.getDownloadUrl()).getHost().equals(host)
                            && feed.getPreferences().getUsername() != null
                            && feed.getPreferences().getPassword() != null) {
                        return feed.getPreferences().getUsername()
                                + ":" + feed.getPreferences().getPassword();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting credentials for URL: " + url, e);
            }
            return null;
        });
        AntennapodHttpClient.setCacheDirectory(new File(context.getCacheDir(), "okhttp"));
        AntennapodHttpClient.setProxyConfig(UserPreferences.getProxyConfig());
        SleepTimerPreferences.init(context);
        NotificationUtils.createChannels(context);
        initialized = true;
    }
}
