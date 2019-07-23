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
		��������ֵ
	*/
	enum ResultCode {
		RC_FAILED = -1,
		RC_OK = 0, // �ɹ�
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
			��ȡ���������ݺ�֪ͨ�ͻ���
			������
				sensor - ��ȡ�����LT602�����״�
				data - ��������
				length - �������ݵĸ���
		*/
		virtual void onDistance(Sensor *sensor, const uint16 *data, int length) = 0;

		/*
			���ݻ�ȡʧ��
			������
				sensor - ��ȡ�����LT602�����״�
				error - �������
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
			����LT602�����״�
			������
				address - LT602�����״��IP��ַ��Ĭ��Ϊ192.168.0.119
				port - �˿ںţ�Ĭ��Ϊ4010
			���أ�
				���ӳɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode connect(const char *address, int port);
		
		ResultCode connect(const char *localIp, int localPort, const char *remoteIp, int remotePort);
		/*
			���û���ʱ��
			������
				time - ʱ��
			���أ�
				���óɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode setIntegrationTime(int time);

		ResultCode setTwiceIntegrationTime(int time1, int time2);

		ResultCode receiveInter(uint8 * frame, int offset, int * extraLength);

		/*
			����LD��λ
			������
				gear - ��λ����4����λ��4������ָʾ�⣬5���ر�ָʾ��
			���أ�
				���óɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode setLD(int gear);

		/*
			��ȡ��ǰ�豸״̬
			������
				state - ���ص�ǰ�豸״̬����������������ֵΪ0
			���أ�
				״̬��ȡ�ɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode getCurrentState(int *state);

		/*
			���LT602�����״��Ƿ�����
			��������
			���أ��Ѿ����ӷ���true�����򷵻�false
		*/
		bool isConnected() const;

		/*
			��ȡLT602�����״��õľ������ݣ�LT602�Ĳ����Ƕȷ�ΧΪ110�ȣ�����320����ľ�������
			������
				buffer - ���ز�õľ�������
				length - ���úͷ��ز�õľ������ݵĸ���
			���أ�
				���ݻ�ȡ�ɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode acquireDistanceData(uint16 *data, int *length);

		/*
			����LT602�����״�ɨ���λ������
			������
				buffer - ɨ���λ������
				length - ɨ���λ�õĸ���
			���أ�
				���ݻ�ȡ�ɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode setPositionData(const uint16 *data, int length);

		/*
			��ȡLT602�����״�ɨ���λ������
			������
				buffer - ����ɨ���λ������
				length - ���úͷ���ɨ���λ�õĸ���
			���أ�
				���ݻ�ȡ�ɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
		*/
		ResultCode acquirePositionData(uint16 *data, int *length);

		/*
			��ʼ�������������ݽ�ͨ��MeasureCallback���ص��ö�
			������
				callback - ���ݻ�ȡ��ص�
			���أ�
				�ɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
			ע�⣺
				�ص���������һ���߳��е��ã���ʵ�ֻص��ķ����в��ܵ���stopMeasureContinuous��disconnect��������������������
		*/
		ResultCode startMeasureContinuous(MeasureCallback *callback);

		/*
			�Ƿ�����������ģʽ
			���أ�
				����������ģʽ����true������false
		*/
		bool isMeasuringContinuous() const;

		/*
			ֹͣ��������
		*/
		void stopMeasureContinuous();

		/*
			�Ͽ���LT602�����״������
		*/
		void disconnect();

		/*
			�����豸

			���أ�
				�ɹ�����RC_OK�����򷵻���Ӧ�Ĵ���ֵ
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
