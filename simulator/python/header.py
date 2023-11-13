features = {'size': {'type': 'benign', 'default': 50, 'keyword': 'SIZE', 'vals':[10,50,100,500]},
            'blocksize': {'type': 'benign', 'default': 1024*1024, 'keyword': 'BLOCK_SIZE', 'vals':[1024*1024]},
            'degree': {'type': 'benign', 'default': 8, 'keyword': 'D', 'vals':[8]},
            'samplesize': {'type': 'benign', 'default': 1024*128, 'keyword': 'SAMPLE_SIZE', 'vals':[1024*128]}}

#protocols to test
config_files = {'GOSSIP': './config/gossipsub.cfg'}


result_dir = './python_logs'


