/*===============================================================================
Copyright (c) 2016-2018 PTC Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/
using UnityEngine;
using UnityEngine.UI;
using Vuforia;
using TMPro;

public class SamplesMainMenu : MonoBehaviour
{

    #region PUBLIC_MEMBERS

    public enum MenuItem
    {
        ImageTargets,
        ModelTargets,
        ModelTargetsTrained,
        GroundPlane,
        VuMark,
        CloudReco,
        ObjectReco,
        MultiTargets,
        CylinderTargets,
        UserDefinedTargets,
        VirtualButtons
    }

    // initialize static enum with one of the items
    public static MenuItem menuItem = MenuItem.ImageTargets;
    public const string MenuScene = "1-Menu";
    public const string LoadingScene = "2-Loading";
    public static bool isAboutScreenVisible;

    #endregion // PUBLIC_MEMBERS


    #region PRIVATE_MEMBERS

    [SerializeField] Canvas aboutCanvas;
    [SerializeField] Text aboutTitle;
    [SerializeField] TextMeshProUGUI aboutDescription;

    AboutScreenInfo aboutScreenInfo;
    SafeAreaManager safeAreaManager;
    Color lightGrey;

    #endregion // PRIVATE_MEMBERS


    #region MONOBEHAVIOUR_METHODS

    void Start()
    {
        this.lightGrey = new Color(220f / 255f, 220f / 255f, 220f / 255f);

        // reset about screen state variable to false when returning from AR scene
        isAboutScreenVisible = false;

        if (this.aboutScreenInfo == null)
        {
            // initialize if null
            this.aboutScreenInfo = new AboutScreenInfo();
        }

        this.safeAreaManager = FindObjectOfType<SafeAreaManager>();

        if (this.safeAreaManager)
        {
            this.safeAreaManager.SetAreaColors(lightGrey, Color.white);
            this.safeAreaManager.SetAreasEnabled(true, true);
        }
    }

    #endregion // MONOBEHAVIOUR_METHODS

    #region PUBLIC_METHODS

    public static string GetSceneToLoad()
    {
        // called by SamplesLoadingScreen to load selected AR scene
        return "3-" + menuItem.ToString();
    }

    public static void LoadScene(string scene)
    {
        UnityEngine.SceneManagement.SceneManager.LoadScene(scene);
    }

    public void BackToMenu()
    {
        // called to return to Menu from About screen
        aboutCanvas.sortingOrder = 0;
        isAboutScreenVisible = false;

        if (this.safeAreaManager)
        {
            this.safeAreaManager.SetAreaColors(lightGrey, Color.white);
            this.safeAreaManager.SetAreasEnabled(true, true);
        }
    }

    public void LoadAboutScene(string itemSelected)
    {
        UpdateConfiguration(itemSelected);

        // This method called from list of Sample App menu buttons
        switch (itemSelected)
        {
            case ("ImageTargets"):
                menuItem = MenuItem.ImageTargets;
                break;
            case ("ModelTargets"):
                menuItem = MenuItem.ModelTargets;
                break;
            case ("ModelTargetsTrained"):
                menuItem = MenuItem.ModelTargetsTrained;
                break;
            case ("GroundPlane"):
                menuItem = MenuItem.GroundPlane;
                break;
            case ("VuMark"):
                menuItem = MenuItem.VuMark;
                break;
            case ("CloudReco"):
                menuItem = MenuItem.CloudReco;
                break;
            case ("ObjectReco"):
                menuItem = MenuItem.ObjectReco;
                break;
            case ("MultiTargets"):
                menuItem = MenuItem.MultiTargets;
                break;
            case ("CylinderTargets"):
                menuItem = MenuItem.CylinderTargets;
                break;
            case ("UserDefinedTargets"):
                menuItem = MenuItem.UserDefinedTargets;
                break;
            case ("VirtualButtons"):
                menuItem = MenuItem.VirtualButtons;
                break;
        }

        LoadingScreen.SceneToLoad = "3-" + menuItem.ToString();

        this.aboutTitle.text = this.aboutScreenInfo.GetTitle(menuItem.ToString());
        this.aboutDescription.text = this.aboutScreenInfo.GetDescription(menuItem.ToString());

        this.aboutCanvas.transform.parent.transform.position = Vector3.zero; // move canvas into position
        this.aboutCanvas.sortingOrder = 2; // bring canvas in front of main menu
        isAboutScreenVisible = true;

        if (this.safeAreaManager)
        {
            this.safeAreaManager.SetAreaColors(this.lightGrey, Color.clear);
            this.safeAreaManager.SetAreasEnabled(true, false);
        }
    }

    void UpdateConfiguration(string scene)
    {
        VuforiaConfiguration.Instance.Vuforia.MaxSimultaneousImageTargets = scene == "VuMarks" ? 10 : 4;
    }

    #endregion // PUBLIC_METHODS

}
