package com.yingke.mediacodec.compose.ui;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yingke.mediacodec.BaseFragment;
import com.yingke.mediacodec.R;
import com.yingke.mediacodec.compose.AudioCodec;
import com.yingke.mediacodec.compose.OnAudioCodecListener;
import com.yingke.mediacodec.compose.OnEncoderListener;
import com.yingke.mediacodec.connect.MediaMuxerThread;
import com.yingke.mediacodec.connect.VideoInfo;
import com.yingke.mediacodec.player.PlayerLog;
import com.yingke.mediacodec.preview.audio.AudioDialog;
import com.yingke.mediacodec.preview.video.PreviewVideoActivity;
import com.yingke.mediacodec.recorder.MediaCodecRecorderActivity;
import com.yingke.mediacodec.utils.FileUtils;
import com.yingke.mediacodec.utils.ScreenUtils;
import com.yingke.mediacodec.utils.ToastUtil;
import com.yingke.mediacodec.widget.dialog.DialogManager;
import com.yingke.mediacodec.widget.localmedia.adapter.BaseLocalMediaAdapter;
import com.yingke.mediacodec.widget.localmedia.config.LocalMediaConfig;
import com.yingke.mediacodec.widget.localmedia.config.MediaConfig;
import com.yingke.mediacodec.widget.localmedia.config.MediaMimeType;
import com.yingke.mediacodec.widget.localmedia.entity.LocalMediaFolder;
import com.yingke.mediacodec.widget.localmedia.entity.LocalMediaResource;
import com.yingke.mediacodec.widget.localmedia.manager.LocalMediaDataManager;
import com.yingke.mediacodec.widget.localmedia.mvp.ILocalMediaView;
import com.yingke.mediacodec.widget.localmedia.mvp.LocalMediaPresenter;
import com.yingke.mediacodec.widget.localmedia.views.GridSpacingItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.yingke.mediacodec.recorder.MediaCodecRecorderActivity.RECORD_OUTPUT_PATH;

/**
 * ?????????
 * </p>
 * <p>Copyright corp.xxx.com 2019 All right reserved </p>
 *
 * @author tuke ?????? 2019/8/22
 * @email
 * <p>
 * ?????????????????????
 * <p>
 */
public class AudioMixFragment extends BaseFragment implements ILocalMediaView , BaseLocalMediaAdapter.OnLocalMediaListener {

    public static final String TAG = "AudioMixFragment";
    public static final int REQUEST_CODE_RECORD = 100;

    public static final int MODE_MIX_MULTI_AUDIO = 0;
    public static final int MODE_SEPERATE_AUDIO = 1;
    public static final int MODE_PCM_TO_AUDIO = 2;

    private View mRootView;
    private RecyclerView mRecyclerView;
    private BaseLocalMediaAdapter mMediaAdapter;

    private List<LocalMediaResource> mSelectedMedias;

    private DialogManager mDialogManager;

    // ??????????????????
    private LocalMediaPresenter mMediaPresenter;

    public int mCurrentMode = MODE_MIX_MULTI_AUDIO;

    public static AudioMixFragment newInstance() {
        return new AudioMixFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.frag_audio_mix, null);
            initViews();
            initDatas();
        }

        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    public void initViews() {
        PlayerLog.e(TAG, "---initViews---");
        mRecyclerView = mRootView.findViewById(R.id.recyclerview);
    }

    public void initDatas() {
        PlayerLog.e(TAG, "---initDatas---");

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(4, ScreenUtils.dip2px(getContext(), 2), false));
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        mMediaAdapter = new BaseLocalMediaAdapter(getContext());
        mMediaAdapter.setLocalMediaListener(this);
        mRecyclerView.setAdapter(mMediaAdapter);

        mSelectedMedias = new ArrayList<>();
        mDialogManager = new DialogManager();

        setCurrentMode(MODE_MIX_MULTI_AUDIO);
        getMediaPresenter().requestMedias();
    }

    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * @return ????????????
     */
    private LocalMediaPresenter getMediaPresenter() {
        if (mMediaPresenter == null) {
            mMediaPresenter = new LocalMediaPresenter(getActivity(), this);
        }
        return mMediaPresenter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onLocalMediaLoaded(List<LocalMediaFolder> localMediaFolders) {
        List<LocalMediaResource> currentMedias = localMediaFolders.get(0).getFolderMedias();
        LocalMediaDataManager.getInstance().setCurrentFolderMedias(currentMedias);
        mMediaAdapter.setDataList(LocalMediaDataManager.getInstance().getCurrentFolderMedias());
        mMediaAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLocalMediaErr(String message) {
        ToastUtil.showToastShort(message);
    }

    @Override
    public void onTakePhotoOrRecord() {
        if (LocalMediaConfig.getInstance().getMediaType() == MediaConfig.MediaType.MEDIA_TYPE_VIDEO) {
//            MediaCodecRecorderActivity.startForResult(getContext(), REQUEST_CODE_RECORD);
        }

    }

    @Override
    public void onSelectMediaChanged(List<LocalMediaResource> selectImages) {
       if (selectImages != null && selectImages.size() > 0) {
           mSelectedMedias.clear();
           mSelectedMedias.addAll(selectImages);
       }
    }

    @Override
    public void onLocalMediaClick(LocalMediaResource media, int position, MediaConfig.MediaType mediaType) {
        if (mediaType == MediaConfig.MediaType.MEDIA_TYPE_AUDIO) {
            AudioDialog audioDialog = new AudioDialog(getContext(), media.getMediaPath());
            audioDialog.show();
        }
    }

    /**
     * ?????????
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_CODE_RECORD
                && resultCode == RESULT_OK && data != null) {
            String mediaPath = data.getStringExtra(RECORD_OUTPUT_PATH);
//            if (!TextUtils.isEmpty(mediaPath)) {
//                LocalMediaResource newResource = getNewMediaResource(mediaPath);
//                mMediaAdapter.addData(0, newResource);
//                mMediaAdapter.notifyDataSetChanged();
//            }
        }
    }

    /**
     * ??????????????????
     * @param currentMode
     */
    public void setCurrentMode(int currentMode) {
        mCurrentMode = currentMode;
        switch (mCurrentMode) {
            case MODE_MIX_MULTI_AUDIO:
                LocalMediaConfig.getInstance().setMediaType(MediaConfig.MediaType.MEDIA_TYPE_AUDIO)
                        .setSelectionMode(MediaConfig.SelectionMode.MULTI_SELECTION);
                break;
            case MODE_SEPERATE_AUDIO:
                break;
            case MODE_PCM_TO_AUDIO:
                break;
        }


    }

    /**
     * ??????????????????
     */
    public void mixMultiAudio() {
        if (mSelectedMedias.size() > 0) {
            String audioPathOne = mSelectedMedias.get(0).getMediaPath();
            String audioPathTwo = mSelectedMedias.get(1).getMediaPath();

            final String outputPath = FileUtils.getComposeAudioOutputFile("mix",".aac").getAbsolutePath();
            // ????????????
            mDialogManager.showDialog(getContext());
            // ????????????
            AudioCodec.createAudioMix(audioPathOne, audioPathTwo, outputPath, new OnEncoderListener() {
                @Override
                public void onEncodeSuc() {
                    ToastUtil.showToastShort("???????????????" + outputPath);
                    // ????????????
                    mDialogManager.dismissDialog();
                    File outputFile = new File(outputPath);
                    if (outputFile.exists()) {
                        // ??????????????????
                        if (!TextUtils.isEmpty(outputPath)) {
                            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
                        }

                        LocalMediaResource newResource = getNewMediaResource(outputPath);
                        // ???????????????
                        mMediaAdapter.addData(0, newResource);
                        // ??????????????????
                        LocalMediaDataManager.getInstance().clearSelectedMedias();
                        mMediaAdapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onEncodeErr() {
                    mDialogManager.dismissDialog();
                    ToastUtil.showToastShort("????????????");
                }
            });

        } else{
            ToastUtil.showToastShort("???????????????");
        }
    }

    /**
     * ?????????????????????
     */
    public void seperateAudio() {

    }

    /**
     * ???pcm??????????????? aac
     */
    public void switchPcmToAudio() {


    }



    /**
     * ????????????video
     */
    public void mergeMultiVideo() {
        if (mSelectedMedias.size() > 0) {
            List<VideoInfo> inputVideos = new ArrayList<>();
            inputVideos.clear();
            for (LocalMediaResource resource : mSelectedMedias) {
               VideoInfo videoInfo = getNewVideoInfo(resource.getMediaPath());
               inputVideos.add(videoInfo);
            }

            final String outputPath = FileUtils.getMergeOutputFile("merge",".mp4").getAbsolutePath();
            MediaMuxerThread mediaMuxerThread = new MediaMuxerThread();
            mediaMuxerThread.setVideoFiles(inputVideos, outputPath);
            mediaMuxerThread.setListener(new MediaMuxerThread.ProcessListener() {
                @Override
                public void onStart() {
                    // ????????????
                    mDialogManager.showDialog(getContext());
                }

                @Override
                public void onFinish() {
                    // ????????????
                    mDialogManager.dismissDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToastShort("???????????????" + outputPath);
                            File outputFile = new File(outputPath);
                            if (outputFile.exists()) {
                                // ??????????????????
                                if (!TextUtils.isEmpty(outputPath)) {
                                    getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outputFile)));
                                }
                                mRecyclerView.smoothScrollToPosition(0);

                                LocalMediaResource newResource = getNewMediaResource(outputPath);
                                // ???????????????
                                mMediaAdapter.addData(0, newResource);
                                // ??????????????????
                                LocalMediaDataManager.getInstance().clearSelectedMedias();
                                mMediaAdapter.notifyDataSetChanged();

                            }
                        }
                    });
                }
            });
            mediaMuxerThread.start();

        } else {
            ToastUtil.showToastShort("???????????????");
        }
    }

    /**
     * ?????? ?????????
     * @param mediaPath
     * @return
     */
    public LocalMediaResource getNewMediaResource(String mediaPath) {
        if (TextUtils.isEmpty(mediaPath)) {
            return null;
        }

        // ???????????????
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mediaPath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

        LocalMediaResource newResource = new LocalMediaResource();
        newResource.setMediaPath(mediaPath);
        newResource.setMimeType(mime);
        newResource.setDuration(Integer.parseInt(duration));

        return newResource;

    }


    /**
     * ?????? videoinfo
     * @param mediaPath
     * @return
     */
    public VideoInfo getNewVideoInfo(@NonNull String mediaPath) {

        VideoInfo videoInfo = new VideoInfo();
        videoInfo.setPath(mediaPath);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mediaPath);
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        PlayerLog.e(TAG, "rotation = " + rotation
                + " width = " + width
                + " height = " + height
                + " duration = " + duration);

        videoInfo.setRotation(Integer.parseInt(rotation));
        videoInfo.setWidth(Integer.parseInt(width));
        videoInfo.setHeight(Integer.parseInt(height));
        videoInfo.setDuration(Integer.parseInt(duration));

        return videoInfo;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getMediaPresenter() != null) {
            getMediaPresenter().onDestroy();
        }
        // ????????????
        LocalMediaDataManager.getInstance().clearSelectedMedias();
        LocalMediaDataManager.getInstance().clearCurrentFolderMedia();
        LocalMediaConfig.getInstance().reset();
        mRootView = null;
    }
}
