#ifndef LT602_SENSOR_H_
#define LT602_SENSOR_H_


#include <pthread.h>

#include <sys/socket.h>
#include <sys/un.h>
#include <sys/endian.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>


#define THREAD_INIT_HANDLE   (-1)
#define THREAD_SUCC_HANDLE   (0)

#define INVALID_SOCKET  	 (-1)

namespace lt602 {

	typedef unsigned char uint8;
	typedef unsigned short uint16;
	/*
		函数返回值
	*/
	enum ResultCode {
		RC_FAILED = -1,
		RC_OK = 0, // 成功
		RC_INVALID_ARGUMENT,		
		RC_NOT_CONNECTED,
		RC_ALREADY_CONNECTED,
		RC_IN_PROGRESS,
		RC_COMMUNICATION_FAILED,
		RC_INVALID_RESPONSE,
		RC_CONNECTION_CLOSED,
		RC_ADDRESS_BIND_FAILED,
	};

	ResultCode Initialize();

	void Uninitialize();

	class Sensor;

	class MeasureCallback {
	public:
		/*
			获取到距离数据后通知客户端
			参数：
				sensor - 获取距离的LT602激光雷达
				data - 距离数据
				length - 距离数据的个数
		*/
		virtual void onDistance(Sensor *sensor, const uint16 *data, int length) = 0;

		/*
			数据获取失败
			参数：
				sensor - 获取距离的LT602激光雷达
				error - 错误代码
		*/
		virtual void onError(Sensor *sensor, ResultCode error) = 0;

		virtual ~MeasureCallback() {
			
		}
	};

	class Sensor {
	public:
		Sensor();

		virtual ~Sensor();

		/*
			连接LT602激光雷达
			参数：
				address - LT602激光雷达的IP地址，默认为192.168.0.119
				port - 端口号，默认为4010
			返回：
				连接成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode connect(const char *address, int port);
		
		ResultCode connect(const char *localIp, int localPort, const char *remoteIp, int remotePort);
		/*
			设置积分时间
			参数：
				time - 时间
			返回：
				设置成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode setIntegrationTime(int time);

		ResultCode setTwiceIntegrationTime(int time1, int time2);

		ResultCode receiveInter(uint8 * frame, int offset, int * extraLength);

		/*
			设置LD挡位
			参数：
				gear - 挡位，共4个挡位，4：开启指示光，5：关闭指示光
			返回：
				设置成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode setLD(int gear);

		/*
			获取当前设备状态
			参数：
				state - 返回当前设备状态，设置正常工作该值为0
			返回：
				状态获取成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode getCurrentState(int *state);

		/*
			检查LT602激光雷达是否连接
			参数：无
			返回：已经连接返回true，否则返回false
		*/
		bool isConnected() const;

		/*
			获取LT602激光雷达测得的距离数据，LT602的测量角度范围为110度，返回320个点的距离数据
			参数：
				buffer - 返回测得的距离数据
				length - 设置和返回测得的距离数据的个数
			返回：
				数据获取成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode acquireDistanceData(uint16 *data, int *length);

		/*
			设置LT602激光雷达扫描点位置数据
			参数：
				buffer - 扫描点位置数据
				length - 扫描点位置的个数
			返回：
				数据获取成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode setPositionData(const uint16 *data, int length);

		/*
			获取LT602激光雷达扫描点位置数据
			参数：
				buffer - 返回扫描点位置数据
				length - 设置和返回扫描点位置的个数
			返回：
				数据获取成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode acquirePositionData(uint16 *data, int *length);

		/*
			开始连续测量，数据将通过MeasureCallback返回调用端
			参数：
				callback - 数据获取后回调
			返回：
				成功返回RC_OK，否则返回相应的错误值
			注意：
				回调将在另外一个线程中调用，在实现回调的方法中不能调用stopMeasureContinuous和disconnect函数，那样将导致死锁
		*/
		ResultCode startMeasureContinuous(MeasureCallback *callback);

		/*
			是否在连续测量模式
			返回：
				在连续测量模式返回true，否则false
		*/
		bool isMeasuringContinuous() const;

		/*
			停止连续测量
		*/
		void stopMeasureContinuous();

		/*
			断开与LT602激光雷达的连接
		*/
		void disconnect();

		/*
			重启设备

			返回：
				成功返回RC_OK，否则返回相应的错误值
		*/
		ResultCode reset();

	private:
		enum {
			MAX_DATA_LENGTH = 643,
			MAX_FRAME_LENGTH = 5 + MAX_DATA_LENGTH + 1
		};
		
		enum {
			//TIME_OUT = 2000
			TIME_OUT = 2
		};

		int				mSocket;
		int				mMeasureThreadHandle;
		pthread_t 		mMeasureThread;
		bool				mMeasureContinuous;
		struct sockaddr_in	mSensorAddress;
		MeasureCallback		*mMeasureCallback;
		uint8				*mFrameBuffer;

		void measureContinuous();

		static void *MeasureThreadProc(void *arg) {
			Sensor *ths = (Sensor *)(arg);

			ths->measureContinuous();
			return nullptr;
		}

		ResultCode sendCommand(const uint8 *data, int size);

		ResultCode receiveFrame(uint8 *frame, int offset, int *extraLength);

		int syncFrame(uint8 *frame, int length, int start);
	};

} // namespace lt602

#endif // LT602_SENSOR_H_
