using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
using System.IO;

public class ButtonListeners:MonoBehaviour
{
    public static bool IsNavigating;

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

    [SerializeField]
    private Image _imgBtnNavigating;

    [SerializeField]
    private GameObject _ssungMaTelling;

    [SerializeField]
    private GameObject _navigatingObject;

    [SerializeField]
    private GameObject _nextButton;
    [SerializeField]
    private GameObject _previousButton;

    private bool _isOpened;
    private Vector3 _ssungPos;

    [SerializeField]
    private Text _text;
    [SerializeField]
    private Text _text2;

    //[SerializeField]
    private string[] _speechTexts = { "0", "1", "2", "3", "4" };

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
        _text = GameObject.FindGameObjectWithTag("BuildingInformationText").GetComponent<Text>();

        if(animator != null)
        {
            _imgBuilding.sprite = Resources.Load<Sprite>("UI/Buildings/" + name);
            _imgName.sprite = Resources.Load<Sprite>("UI/Building Names/" + name + "UI");

            StreamReader sr = new StreamReader("Assets/Resources/Building Informations/" + name + ".txt", System.Text.Encoding.Default);
            _text.text = sr.ReadLine();

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

            StartCoroutine(MoveSsungMa(animator, "Run"));
        }
    }

    IEnumerator MoveSsungMa(Animator animator, string state)
    {
        _ssungPos = _panel.transform.position;

        if(state == "Run")
        {
            while(_panel.transform.position.x > 670)
            {
                yield return new WaitForSeconds(0.01f);
                _ssungPos.x -= 0.78f;
                _panel.transform.position = new Vector3(_ssungPos.x, _ssungPos.y, _ssungPos.z);
            }
            animator.SetBool("Run", false);
            Tell(true, true);
        }
        else if(state == "Walk")
        {
            GameObject ssungMaTelling = GameObject.Find("SSung Ma Telling");
            GameObject questionMark = GameObject.Find("Question Mark");

            // 말풍선 숨기기
            ssungMaTelling.transform.localScale = new Vector3(0, 0, 0);
            // 슝마 이동
            animator.SetBool("Walk", true);
            while(_panel.transform.position.x > 600)
            {
                yield return new WaitForSeconds(0.01f);
                _ssungPos.x -= 0.2f;
                _panel.transform.position = new Vector3(_ssungPos.x, _ssungPos.y, _ssungPos.z);
            }
            animator.SetBool("Walk", false);
            _panel.SetActive(false);
            // 물음표 복구
            questionMark.transform.position = new Vector3(669.2f, 8, 529.9f);
            gameObject.SetActive(false);
        }
    }

    public void Tell(bool isFirstPage, bool isNext)
    {
        for(int i = 0; i < _speechTexts.Length; i++)
        {
            StreamReader sr = new StreamReader("Assets/Resources/SSung Ma Telling/" + Convert.ToString(i) + ".txt", System.Text.Encoding.UTF8);
            _speechTexts[i] = sr.ReadLine();
        }

        if(isFirstPage)
        {
            // 말풍선이 생성되었을 때
            _ssungMaTelling.transform.localScale = new Vector3(1.3f, 1.3f, 1.3f);
            _text.text = _speechTexts[0];
            _text2.text = "0";
        }
        else
        {
            int index = Convert.ToInt32(_text2.text);
            // 다음 말풍선
            if(isNext)
            {
                int nextIndex = index + 1;
                if(nextIndex < 5)
                {
                    _text.text = _speechTexts[nextIndex];
                    _text2.text = Convert.ToString(nextIndex);
                    _previousButton.transform.localScale = new Vector3(1, 1, 1);

                    if(nextIndex == 4)
                    {
                        // 다음버튼 비활성화
                        _nextButton.transform.localScale = new Vector3(0, 0, 0);
                        // 안녕버튼 활성화
                        GameObject.Find("SSung Ma Telling").transform.Find("Bye SSung Ma").gameObject.SetActive(true);
                    }
                }
            }
            // 이전 말풍선
            else
            {
                int prevIndex = index - 1;
                if(prevIndex > -1)
                {
                    _text.text = _speechTexts[prevIndex];
                    _text2.text = Convert.ToString(prevIndex);
                    _nextButton.transform.localScale = new Vector3(1, 1, 1);
                    // 안녕버튼 비활성화
                    GameObject.Find("SSung Ma Telling").transform.Find("Bye SSung Ma").gameObject.SetActive(false);

                    if(prevIndex == 0)
                    {
                        // 이전버튼 비활성화
                        _previousButton.transform.localScale = new Vector3(0, 0, 0);
                    }
                }
            }
        }
    }

    public void OnClickNextOnSpeechBubble()
    {
        Tell(false, true);
    }

    public void OnClickPrevOnSpeechBubble()
    {
        Tell(false, false);
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
                ARnavigating.ClearNavi();
                _navigatingObject.SetActive(false);
                IsNavigating = false;
            }
            else
            {
                // 네비게이팅을 실행시키기 위해 목적지 창 띄우기(실제 실행은 아님)
                _text.text = "";
                animator.SetBool("open", true);
            }
        }
    }

    public void OnClickDestination()
    {
        _text = GameObject.FindGameObjectWithTag("Destination").GetComponent<Text>();

        switch(name)
        {
            case "Baird":
                _text.text = "베어드홀";
                break;
            case "SoongDeok":
                _text.text = "숭덕관";
                break;
            case "MoonHwa":
                _text.text = "문화관";
                break;
            case "IkTae":
                _text.text = "안익태기념관";
                break;
            case "HyeongNam":
                _text.text = "형남공학관";
                break;
            case "KyoYook":
                _text.text = "교육관";
                break;
            case "BaekMa":
                _text.text = "백마관";
                break;
            case "KyeongJik":
                _text.text = "한경직기념관";
                break;
            case "SinYang":
                _text.text = "신양관";
                break;
            case "Venture":
                _text.text = "벤처중소기업센터";
                break;
            case "JinLee":
                _text.text = "진리관";
                break;
            case "ManSik":
                _text.text = "조만식기념관";
                break;
            case "Museum":
                _text.text = "한국기독교박물관";
                break;
            case "Library":
                _text.text = "중앙도서관";
                break;
            case "Research":
                _text.text = "연구관";
                break;
            case "ChangSin":
                _text.text = "창신관";
                break;
            case "Global":
                _text.text = "글로벌브레인홀";
                break;
            case "Residence":
                _text.text = "레지던스홀";
                break;
            case "JeonSan":
                _text.text = "전산관";
                break;
            case "MiRae":
                _text.text = "미래관";
                break;
            case "JeongBo":
                _text.text = "정보과학관";
                break;
            case "West":
                _text.text = "웨스트민스터홀";
                break;
            case "Student":
                _text.text = "학생회관";
                break;
            case "ChangUi":
                _text.text = "창의관";
                break;
            default:
                break;
        }
    }

    public void OnClickStartNavigating()
    {
        if(!IsNavigating)
        {
            _text = GameObject.FindGameObjectWithTag("Destination").GetComponent<Text>();

            switch(_text.text)
            {
                case "베어드홀":
                    ARnavigating.Destination = 0;
                    break;
                case "학생회관":
                    ARnavigating.Destination = 1;
                    break;
                case "진리관":
                    ARnavigating.Destination = 2;
                    break;
                case "신양관":
                    ARnavigating.Destination = 4;
                    break;
                case "한경직기념관":
                    ARnavigating.Destination = 5;
                    break;
                default:
                    _text.text = "길찾기 실패";
                    break;
            }

            if(_text.text != "길찾기 실패")
            {
                // 네비게이팅 서비스 시작
                _imgBtnNavigating.sprite = Resources.Load<Sprite>("UI/navigation exit button1");
                _navigatingObject.SetActive(true);
                IsNavigating = true;

                Animator animator = _panel.GetComponent<Animator>();
                animator.SetBool("open", false);
            }
        }
    }

    public void OnClickClubMenu()
    {
        _text = GameObject.FindGameObjectWithTag("ClubInfoText").GetComponent<Text>();

        _text.text = "";
        Animator animator = _panel.GetComponent<Animator>();
        if(animator != null)
        {
            animator.SetBool("open", true);
            int index = Convert.ToInt32(name);
            _text.text = GroupParsing.separationRecords[index].separation + "\n\n\n";
            for(int i = 0; i < GroupParsing.separationRecords[index].element.Count; i++)
            {
                _text.text += "\n" + GroupParsing.separationRecords[index].element[i].name + " -> ";
                _text.text += GroupParsing.separationRecords[index].element[i].location + "\n";
                _text.text += GroupParsing.separationRecords[index].element[i].information + "\n";
            }
        }
    }

    public void OnClickByeSsungMa()
    {
        Animator animator = _panel.GetComponent<Animator>();

        if(animator != null)
        {
            StartCoroutine(MoveSsungMa(animator, "Walk"));
        }
    }
}
