using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonListeners : MonoBehaviour
{
    [SerializeField]
    private GameObject _panel;

    [SerializeField]
    private Image _imgBuilding;
    [SerializeField]
    private Image _imgName;

    [SerializeField]
    private GameObject _btnClub;
    [SerializeField]
    private GameObject _btnRestaurant;
    [SerializeField]
    private GameObject _btnBuilding;

    private bool _isOpened;

    public void OnClickExit()
    {
        Animator animator = _panel.GetComponent<Animator>();
        if(animator != null)
            animator.SetBool("open", false);
    }

    public void OnClickOpenBuildingInfo()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            _imgBuilding.sprite = Resources.Load<Sprite>("UI/Buildings/" + name);
            _imgName.sprite = Resources.Load<Sprite>("UI/Building Names/" + name + "UI");

            animator.SetBool("open", true);
        }
    }

    public void OnClickOpenRestaurantInfo()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            animator.SetBool("open", true);
        }
    }

    public void OnClickStart()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            animator.SetBool("start", true);
        }
    }

    public void OnClickStudent()
    {
        _isOpened = !_isOpened;

        Animator animClub = _btnClub.GetComponent<Animator>();
        Animator animRestaurant = _btnRestaurant.GetComponent<Animator>();
        Animator animBuilding = _btnBuilding.GetComponent<Animator>();

        animClub.SetBool("open", _isOpened);
        animRestaurant.SetBool("open", _isOpened);
        animBuilding.SetBool("open", _isOpened);
    }

    public void OnClickQuestionMark()
    {

    }
}
