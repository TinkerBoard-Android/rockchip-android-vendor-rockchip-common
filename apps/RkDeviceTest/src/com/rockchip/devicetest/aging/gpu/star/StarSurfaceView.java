/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月15日 下午5:40:45  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月15日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging.gpu.star;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.rockchip.devicetest.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.KeyEvent;

public class StarSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
	


	public StarSurfaceView(Context context) {
		super(context);
		GLImage.load(getResources());
		this.setRenderer(this);
        //Request focus, otherwise buttons won't react
        this.requestFocus();
        this.setFocusableInTouchMode(true);
	}

		private int one = 0x10000;
		private IntBuffer vertexBuffer;
		private int[] vertex = new int[] { -one, -one, 0, one, -one, 0, -one, one,
				0, one, one, 0 };
		private IntBuffer coordBuffer;
		private int[] coord = { 0, 0, one, 0, 0, one, one, one };
		private int[] textures = new int[1];
		private Random random = new Random();
		// 闪烁的星星
		boolean twinkle = true;
		// star数目
		int num = 50;
		// star数目数组
		NiceStar[] star = new NiceStar[num];
		// star 倾角
		float tilt = 90.0f;
		// star 距人的dist
		float zoom = -10.0f;
		float spin; // 闪烁星星的自转
		int PortWidth;
		int PortHeight;

		// init数据
		public void initData() {

			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = byteBuffer.asIntBuffer();
			vertexBuffer.put(vertex);
			vertexBuffer.position(0);

			ByteBuffer coordByteBuffer = ByteBuffer
					.allocateDirect(coord.length * 4);
			coordByteBuffer.order(ByteOrder.nativeOrder());
			coordBuffer = coordByteBuffer.asIntBuffer();
			coordBuffer.put(coord);
			coordBuffer.position(0);
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			// 清除屏幕和深度缓存
			gl.glClear(GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_COLOR_BUFFER_BIT);
			// 初始化数据
			initData();

			for (int i = 0; i < num; i++) {
				gl.glLoadIdentity();
				// 向屏幕里移入zoom
				gl.glTranslatef(0.0f, 0.0f, zoom);


				// 开启顶点、颜色和纹理
				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

				gl.glTranslatef(star[i].x, star[i].y, 0.0f);
				if (twinkle) // 启用闪烁效果
				{
					// 使用byte型数值指定一个颜色
					gl.glColor4f((float) star[(num - i) - 1].r / 255.0f,
							(float) star[(num - i) - 1].g / 255.0f,
							(float) star[(num - i) - 1].b / 255.0f, 1.0f);
					gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
					gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, coordBuffer);
					// 绘制
					gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
					// 绘制结束
					gl.glFinish();
				}

				//自转
				 gl.glRotatef(star[i].angle, 0.0f, 0.0f, 1.0f); // 绕z轴旋转星星

				// 使用byte型数值指定一个颜色
				gl.glColor4f((float) star[(num - i) - 1].r / 255.0f,
						(float) star[(num - i) - 1].g / 255.0f,
						(float) star[(num - i) - 1].b / 255.0f, 1.0f);

				gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);
				gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, coordBuffer);
				// 绘制
				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
				// 关闭顶点、颜色和纹理
				gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

				//自转参数
				star[i].angle += (float) (i+1) / (float) num; // 改变星星的自转角度

				// 向下
				star[i].y -= (float) 0.05;

				//循环
				if (star[i].y < -11.0) 
				{
					star[i].y += 22.0f;
					// 赋一个新红色分量
					star[i].r = random.nextInt(256);
					// 赋一个新绿色分量
					star[i].g = random.nextInt(256);
					// 赋一个新蓝色分量
					star[i].b = random.nextInt(256);
				}
			}
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			PortWidth = width;
//			String ans = "";
//			Log.v("PortWidth",ans.valueOf(PortWidth));
			PortHeight = height;
			gl.glViewport(0, 0, width, height);
			float ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
			// 设置观察模型
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
			// 黑色背景色
			gl.glClearColorx(0, 0, 0, 0);
			// 启用阴影平滑
			gl.glShadeModel(GL10.GL_SMOOTH);
			// 启用深度测试
			gl.glEnable(GL10.GL_DEPTH_TEST);
			// 深度测试类型
			gl.glDepthFunc(GL10.GL_LEQUAL);
			// 设置深度缓存
			gl.glClearDepthf(1.0f);

			// 启用纹理
			gl.glEnable(GL10.GL_TEXTURE_2D);
			// 生成纹理
			gl.glGenTextures(1, textures, 0);
			// 绑定纹理
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.mBitmap, 0);

			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); // 设置混色函数取得半透明效果
			gl.glEnable(GL10.GL_BLEND); // 启用混色

			// 初始化 all star
			for (int i = 0; i < num; i++) {
				
				NiceStar starTmp = new NiceStar();
				// all star from 0 angle
				starTmp.angle = 0.0f;
				
				// 随机的坐标
				starTmp.x = (float)(i+1)/(float)num * (float)20.0;
				starTmp.x -= 10.0;
//				String ans = "";
//				Log.v("starTmp", ans.valueOf(starTmp.x));
				starTmp.y = ((float) random.nextInt(22)) ;
				
				// set red
				starTmp.r = random.nextInt(256);
				// set green
				starTmp.g = random.nextInt(256);
				// set blue
				starTmp.b = random.nextInt(256);

				star[i] = starTmp;
			}
		}
	}

	class NiceStar {
		// star颜色
		int r, g, b;
		// star坐标
		float x, y;
		// current star angle
		float angle = 0.0f;
	}
	
	class GLImage
	{
		 public static Bitmap mBitmap;
		 public static void load(Resources resources)
		 {
		  mBitmap = BitmapFactory.decodeResource(resources, R.drawable.stars);
		 }
		}
