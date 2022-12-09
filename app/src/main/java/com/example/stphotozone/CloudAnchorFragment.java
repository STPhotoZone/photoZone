package com.example.stphotozone;

import android.content.Context;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

public class CloudAnchorFragment extends ArFragment {


    @Override
    protected Config onCreateSessionConfig(Session session) {
        // 클라우드 앵커를 사용하도록 설정해둔다.
        Config config = super.onCreateSessionConfig(session);
        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
        return config;
    }
}
