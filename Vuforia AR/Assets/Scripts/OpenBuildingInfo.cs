using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class OpenBuildingInfo : MonoBehaviour
{
    [SerializeField]
    private GameObject _panel;
    [SerializeField]
    private Image _imgBuilding;
    [SerializeField]
    private Image _imgName;

    public void OnClickOpenButton()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            _imgBuilding.sprite = Resources.Load<Sprite>("UI/Buildings/" + name);
            _imgName.sprite = Resources.Load<Sprite>("UI/Building Names/" + name + "UI");

            animator.SetBool("open", true);
        }
    }
}
