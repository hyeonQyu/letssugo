using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonListeners : MonoBehaviour
{
    public static bool IsNavigating;

    [SerializeField]
    private GameObject _panel;

    [SerializeField]
    private Text _clubText;

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

    [SerializeField]
    private Image _imgBtnNavigating;

    [SerializeField]
    private Text _destinationText;

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

    public void OnClickOpenRestaurantClubInfo()
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
        _panel.SetActive(true);
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

        while(_panel.transform.position.x > 670)
        {
            yield return new WaitForSeconds(0.01f);
            _ssungPos.x -= 0.78f;
            _panel.transform.position = new Vector3(_ssungPos.x, _ssungPos.y, _ssungPos.z);
        }
        animator.SetBool("Run", false);
    }

    public void OnClickNavigating()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            if(IsNavigating)
            {
                // 네비게이팅 실행 중 -> 네비게이팅 종료
                _imgBtnNavigating.sprite = Resources.Load<Sprite>("UI/navigation button");
                IsNavigating = false;
            }
            else
            {
                // 네비게이팅을 실행시키기 위해 목적지 창 띄우기(실제 실행은 아님)
                animator.SetBool("open", true);
            }
        }        
    }

    public void OnClickDestination()
    {
        switch(name)
        {
            case "Baird":
                _destinationText.text = "베어드홀";
                break;
            case "SoongDeok":
                _destinationText.text = "숭덕관";
                break;
            case "MoonHwa":
                _destinationText.text = "문화관";
                break;
            case "IkTae":
                _destinationText.text = "안익태기념관";
                break;
            case "HyeongNam":
                _destinationText.text = "형남공학관";
                break;
            case "KyoYook":
                _destinationText.text = "교육관";
                break;
            case "BaekMa":
                _destinationText.text = "백마관";
                break;
            case "KyeongJik":
                _destinationText.text = "한경직기념관";
                break;
            case "SinYang":
                _destinationText.text = "신양관";
                break;
            case "Venture":
                _destinationText.text = "벤처중소기업센터";
                break;
            case "JinLee":
                _destinationText.text = "진리관";
                break;
            case "ManSik":
                _destinationText.text = "조만식기념관";
                break;
            case "Museum":
                _destinationText.text = "한국기독교박물관";
                break;
            case "Library":
                _destinationText.text = "중앙도서관";
                break;
            case "Research":
                _destinationText.text = "연구관";
                break;
            case "ChangSin":
                _destinationText.text = "창신관";
                break;
            case "Global":
                _destinationText.text = "글로벌브레인홀";
                break;
            case "Residence":
                _destinationText.text = "레지던스홀";
                break;
            case "JeonSan":
                _destinationText.text = "전산관";
                break;
            case "MiRae":
                _destinationText.text = "미래관";
                break;
            case "JeongBo":
                _destinationText.text = "정보과학관";
                break;
            case "West":
                _destinationText.text = "웨스트민스터홀";
                break;
            case "Student":
                _destinationText.text = "학생회관";
                break;
            case "ChangUi":
                _destinationText.text = "창의관";
                break;
            default:
                break;
        }
    }

    public void OnClickStartNavigating()
    {
        if(!IsNavigating)
        {
            switch(_destinationText.text)
            {
                case "베어드홀":
                    //ARnavigating.Destination = 0;
                    break;
                case "학생회관":
                    //ARnavigating.Destination = 1;
                    break;
                case "진리관":
                    //ARnavigating.Destination = 2;
                    break;
                case "신양관":
                    //ARnavigating.Destination = 4;
                    break;
                case "한경직기념관":
                    //ARnavigating.Destination = 5;
                    break;
                default:
                    _destinationText.text = "길찾기 실패";
                    break;
            }

            if(_destinationText.text != "길찾기 실패")
            {
                // 네비게이팅 서비스 시작
                Debug.Log("안에서 눌림");
                _imgBtnNavigating.sprite = Resources.Load<Sprite>("UI/navigation exit button");
                IsNavigating = true;

                Animator animator = _panel.GetComponent<Animator>();
                animator.SetBool("open", false);
            }          
        }     
    }

    public void OnClickClubMenu()
    {
        _clubText.text = "";
        Animator animator = _panel.GetComponent<Animator>();
        if(animator != null)
        {
            animator.SetBool("open", true);
            int index = Convert.ToInt32(name);
            _clubText.text = GroupParsing.separationRecords[index].separation + "\n\n\n";
            for(int i = 0; i < GroupParsing.separationRecords[index].element.Count; i++)
            {
                _clubText.text += "\n" + GroupParsing.separationRecords[index].element[i].name + " -> ";
                _clubText.text += GroupParsing.separationRecords[index].element[i].location + "\n";
                _clubText.text += GroupParsing.separationRecords[index].element[i].information + "\n";
            }
        }
    }
}
