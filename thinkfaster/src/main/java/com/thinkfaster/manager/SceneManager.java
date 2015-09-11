package com.thinkfaster.manager;

import com.purplebrain.adbuddiz.sdk.AdBuddiz;
import com.thinkfaster.model.scene.BaseScene;
import com.thinkfaster.model.scene.LoadingScene;
import com.thinkfaster.model.scene.SplashScene;
import com.thinkfaster.model.scene.game.EndGameScene;
import com.thinkfaster.model.scene.game.GameTypeScene;
import com.thinkfaster.model.scene.game.SinglePlayerGameScene;
import com.thinkfaster.model.scene.menu.AboutScene;
import com.thinkfaster.model.scene.menu.HighScoreScene;
import com.thinkfaster.model.scene.menu.MainMenuScene;
import com.thinkfaster.util.*;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.ui.IGameInterface;
import org.andengine.ui.activity.BaseGameActivity;

/**
 * User: Breku
 * Date: 21.09.13
 */
public class SceneManager {

    private static final SceneManager INSTANCE = new SceneManager();

    private BaseGameActivity activity;

    private SceneType currentSceneType = SceneType.SPLASH;
    private BaseScene singlePlayerGameScene, menuScene, loadingScene, splashScene, currentScene,
            aboutScene, optionsScene, endGameScene, recordScene, gameTypeScene;

    public static void prepareManager(BaseGameActivity activity) {
        getInstance().activity = activity;
    }

    public static SceneManager getInstance() {
        return INSTANCE;
    }

    public void createSplashScene(IGameInterface.OnCreateSceneCallback onCreateSceneCallback) {
        ResourcesManager.getInstance().loadSplashScreen();
        splashScene = new SplashScene();
        currentScene = splashScene;
        onCreateSceneCallback.onCreateSceneFinished(splashScene);
    }

    public void createMainMenuScene() {
        ResourcesManager.getInstance().loadMainMenuResources();
        ResourcesManager.getInstance().loadGameTypeResources();
        ResourcesManager.getInstance().loadLoadingResources();
        menuScene = new MainMenuScene();
        loadingScene = new LoadingScene();
        setScene(menuScene);
        disposeSplashScene();
        showAppRating();
    }

    public void setScene(BaseScene scene) {
        ResourcesManager.getInstance().getEngine().setScene(scene);
        currentScene = scene;
        currentSceneType = scene.getSceneType();
    }

    public void loadMenuScene(BaseScene scene) {
        setScene(loadingScene);
        scene.disposeScene();
        ResourcesManager.getInstance().getEngine().registerUpdateHandler(new TimerHandler(ContextConstants.LOADING_SCENE_TIME, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                ResourcesManager.getInstance().getEngine().unregisterUpdateHandler(pTimerHandler);
                ResourcesManager.getInstance().loadMenuTextures();
                ResourcesManager.getInstance().loadGameTypeResources();
                setScene(menuScene);
            }
        }));
    }

    public void loadGameTypeScene() {
        gameTypeScene = new GameTypeScene();
        setScene(gameTypeScene);
        ResourcesManager.getInstance().unloadMenuTextures();
        AdBuddiz.showAd(activity);
    }


    public void loadGameScene(final LevelDifficulty levelDifficulty, final MathParameter mathParameter) {
        setScene(loadingScene);
        ResourcesManager.getInstance().unloadGameTypeTextures();
        ResourcesManager.getInstance().getEngine().registerUpdateHandler(new TimerHandler(ContextConstants.LOADING_SCENE_TIME, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                ResourcesManager.getInstance().getEngine().unregisterUpdateHandler(pTimerHandler);
                ResourcesManager.getInstance().loadGameResources();
                singlePlayerGameScene = new SinglePlayerGameScene(levelDifficulty, mathParameter);
                setScene(singlePlayerGameScene);
            }
        }));
    }

    public void loadAboutScene() {
        setScene(loadingScene);
        ResourcesManager.getInstance().unloadMenuTextures();
        ResourcesManager.getInstance().getEngine().registerUpdateHandler(new TimerHandler(ContextConstants.LOADING_SCENE_TIME / 2, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                ResourcesManager.getInstance().getEngine().unregisterUpdateHandler(pTimerHandler);
                ResourcesManager.getInstance().loadAboutResources();
                aboutScene = new AboutScene();
                setScene(aboutScene);
            }
        }));
    }

    public void loadHighScoreSceneFrom(SceneType sceneType, final Integer score, final LevelDifficulty levelDifficulty, final MathParameter mathParameter) {
        switch (sceneType) {
            case MENU:
                setScene(loadingScene);
                ResourcesManager.getInstance().unloadMenuTextures();
                ResourcesManager.getInstance().getEngine().registerUpdateHandler(new TimerHandler(ContextConstants.LOADING_SCENE_TIME / 2, new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler pTimerHandler) {
                        ResourcesManager.getInstance().getEngine().unregisterUpdateHandler(pTimerHandler);
                        ResourcesManager.getInstance().loadRecordResources();
                        recordScene = new HighScoreScene();
                        setScene(recordScene);
                    }
                }));
                break;
            case SINGLE_PLAYER_GAME:
                setScene(loadingScene);
                singlePlayerGameScene.disposeScene();
                ResourcesManager.getInstance().unloadGameTextures();
                ResourcesManager.getInstance().getEngine().registerUpdateHandler(new TimerHandler(ContextConstants.LOADING_SCENE_TIME / 4, new ITimerCallback() {
                    @Override
                    public void onTimePassed(TimerHandler pTimerHandler) {
                        ResourcesManager.getInstance().getEngine().unregisterUpdateHandler(pTimerHandler);
                        ResourcesManager.getInstance().loadRecordResources();
                        recordScene = new HighScoreScene(score, levelDifficulty, mathParameter);
                        setScene(recordScene);
                    }
                }));
                break;
            case ENDGAME:
                ResourcesManager.getInstance().loadRecordResources();
                recordScene = new HighScoreScene();
                setScene(recordScene);
                break;
            default:
                throw new UnsupportedOperationException();
        }

    }

    public void loadEndGameScene(Integer score) {
        endGameScene = new EndGameScene(score);
        setScene(endGameScene);
        singlePlayerGameScene.disposeScene();
        ResourcesManager.getInstance().unloadGameTextures();
    }


    public BaseScene getCurrentScene() {
        return currentScene;
    }


    private void disposeSplashScene() {
        splashScene.disposeScene();
        splashScene = null;
    }

    private void showAppRating() {
        final BaseGameActivity activity = ResourcesManager.getInstance().getActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppRater.app_launched(activity);
            }
        });
    }
}
