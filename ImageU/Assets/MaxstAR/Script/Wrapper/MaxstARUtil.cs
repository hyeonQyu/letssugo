using UnityEngine;
using System.Collections;
using UnityEngine.Rendering;

namespace maxstAR
{
    public class MaxstARUtil : MonoBehaviour
    {
        public const string ImageTargetTexturePath = "Assets/Editor/MaxstAR/Textures";
        public const string MarkerTargetTexturePath = "Assets/Editor/MaxstAR/Marker";

        public static bool IsDirectXAPI()
        {
            return (SystemInfo.graphicsDeviceType == GraphicsDeviceType.Direct3D9 ||
                SystemInfo.graphicsDeviceType == GraphicsDeviceType.Direct3D12 ||
                SystemInfo.graphicsDeviceType == GraphicsDeviceType.Direct3D11);
        }

        internal const int NATIVE_RENDER_EVENT_DRAW_BG_GL = 0x10000;
        internal const int NATIVE_RENDER_EVENT_DRAW_BG_DX = 0x10001;
        internal const int NATIVE_RENDER_EVENT_DRAW_ZOMBIE_GL = 0x20000;
        internal const int NATIVE_RENDER_EVENT_DRAW_ZOMBIE_DX = 0x20001;

        public static IEnumerator LoadImageFromFileWithSizeAndTexture(string path, System.Action<int, int, Texture2D> complete)
        {
            WWW img_load = new WWW("file://" + path);

            yield return img_load;

            Texture2D texture = (Texture2D)img_load.texture;

            if (complete != null)
            {
                complete(texture.width, texture.height, texture);
            }
        }

        public static string ChangeNewLine(string originalText)
        {
            return originalText.Replace("\\n", "\n");
        }

        public static string DeleteNewLine(string originalText)
        {
            return originalText.Replace("\\n", "");
        }

        public const int TYPE_WIDTH = 0;
        public const int TYPE_HEIGHT = 1;

        public static float GetPixelFromInch(int type, float inch)
        {
            switch (type)
            {
                case TYPE_WIDTH:
                    float wScale = (float)Screen.width / (float)Screen.currentResolution.width;
                    return inch * Screen.dpi / (wScale) * (1920f / Screen.currentResolution.width);

                case TYPE_HEIGHT:
                    float hScale = (float)Screen.height / (float)Screen.currentResolution.height;
                    return inch * Screen.dpi / (hScale) * (1920f / Screen.currentResolution.width);

                default:
                    return inch;
            }
        }

        public static float DeviceDiagonalSizeInInches()
        {
            float screenWidth = Screen.width / Screen.dpi;
            float screenHeight = Screen.height / Screen.dpi;
            float diagonalInches = Mathf.Sqrt(Mathf.Pow(screenWidth, 2) + Mathf.Pow(screenHeight, 2));
            return diagonalInches;
        }
    }
}
