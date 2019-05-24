using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonTest : MonoBehaviour
{
    [SerializeField]
    private GameObject _panel;
    //private PanelAnimation _panelAnim;
    private bool _isPanelActive;

    public void OnClickTestButton()
    {
        Animator animator = _panel.GetComponent<Animator>();
        if(animator != null)
        {
            _isPanelActive = !_isPanelActive;
         //   _panel.SetActive(_isPanelActive);
            animator.SetBool("open", !_isPanelActive);
        }
    }
}
