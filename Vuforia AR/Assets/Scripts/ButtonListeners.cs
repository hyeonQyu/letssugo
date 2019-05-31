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

    [SerializeField]
    private GameObject _questionMark;

    private bool _isOpened;
    private Vector3 _ssungPos;

    public void OnClickExit()
    {
        Animator animator = _panel.GetComponent<Animator>();
        if(animator != null)
            animator.SetBool("open", false);
    }

    public void OnClickOpenBuildingInfo()
    {
        //_panel = 건물 정보 패널
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
        // _panel = 학식 정보 패널
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            animator.SetBool("open", true);
        }
    }

    public void OnClickStart()
    {
        // _panel = 시작 화면
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
        // _panel = 슝마
        //_panel.SetActive(true);
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            _questionMark.transform.position = new Vector3(-100, -100, -100);
            animator.SetBool("Run", true);

            StartCoroutine(MoveSsungMa(animator));
        }
    }

    IEnumerator MoveSsungMa(Animator animator)
    {
        _ssungPos = _panel.transform.position;
        bool isAnimationTrue = true;

        while(_panel.transform.position.x > 685)
        {
            yield return new WaitForSeconds(0.01f);

           // if(_ssungPos.x > 705)
            _ssungPos.x -= 0.3f;
            //else if(_ssungPos.x > 695)
            //{
            //    if(isAnimationTrue)
            //        animator.SetBool("Run", false);
            //    _ssungPos.x -= 1f;
            //}
            //else
            //    _ssungPos.x -= 0.5f;

            _panel.transform.position = new Vector3(_ssungPos.x, _ssungPos.y, _ssungPos.z);
        }
        animator.SetBool("Run", false);
    }
}
