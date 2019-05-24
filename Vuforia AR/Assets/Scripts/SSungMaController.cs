using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class SSungMaController : MonoBehaviour
{
    [SerializeField]
    private GameObject _ssungMa;
    private bool _isWalk;

    public void OnClickSSungMa()
    {
        Animator animator = _ssungMa.GetComponent<Animator>();
        if(animator != null)
        {
            _isWalk = !_isWalk;
            animator.SetBool("Walk", _isWalk);
        }
    }
}
